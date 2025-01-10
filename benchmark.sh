#!/bin/bash

# fichier 1Go => hash: cd573cfaace07e7949bc0c46028904ff
# fichier 3Go => hash: c698c87fb53058d493492b61f4c74189
DL="cd573cfaace07e7949bc0c46028904ff"
CLIENT_COUNTS="10 5 3 1"
INITIALIZE=1

# Source aws credentials
source .env


#
# Utils
#
function echo_status {
	if [ $1 -eq 1 ]; then
		tput cuu 1 && tput el
		echo -e "$2 \033[0;32mOK\033[0m"
	else
		echo -e "$2"
	fi
}

function run_step {
	echo_status 0 "$1"
	eval $2
	echo_status 1 "$1"
}


#
# Benchmark
#
run_step "Building the project..." "./gradlew clean jar > /dev/null 2>&1"

# Starting benchmark
OUTPUT=$(mktemp)
RESULTS=$(mktemp)

echo -e "Clients\tTime\tSpeed" > $RESULTS

for client_count in $CLIENT_COUNTS; do
	echo_status 0 "Updating the infrastructure ($client_count clients)..."
	pushd infra > /dev/null
	DIARY_IP=$(tofu apply -auto-approve -var "client_count=$client_count" 2>&1 | sed -n 's/.*diary_ip = "\([^"]*\)".*/\1/p')
	popd > /dev/null
	echo_status 1 "Updating the infrastructure..."

	if [ $INITIALIZE -eq 1 ]; then
		echo_status 0 "Waiting everything to be up and running (initial deployment)..."
		while [ $(ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null ec2-user@$DIARY_IP sudo docker logs diary 2> /dev/null | wc -l) -ne $(($client_count*2+1)) ]; do
			sleep 5
		done
		INITIALIZE=0
		echo_status 1 "Waiting everything to be up and running..."
	fi

	run_step "Benchmarking with $client_count clients..." "java -jar app/build/libs/app.jar $DIARY_IP 4000 --no-tui --dl $DL > $OUTPUT 2>&1"

	# Extracting the results
	echo -e "$client_count\t$(grep -oP 'Download of .* took \K[0-9]+' $OUTPUT)\t$(grep -oP 'at a speed of \K[0-9]+' $OUTPUT)B/s" >> $RESULTS
done
rm $OUTPUT

pushd infra > /dev/null
run_step "Destroying the infrastructure..." "tofu destroy -auto-approve > /dev/null 2>&1"
popd > /dev/null

# show results

cat $RESULTS | column -t
cat $RESULTS | column -t > results.txt
rm $RESULTS

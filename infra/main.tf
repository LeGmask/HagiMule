variable "client_count" {
  default = 1
}

resource "aws_instance" "diary" {
  ami                         = "ami-0d16a00c70ee279b8" # Amazon Linux 2 AMI
  instance_type               = "t2.micro"
  associate_public_ip_address = true
  user_data                   = <<-EOF
    #!/bin/bash
    yum update -y
    amazon-linux-extras install docker -y
    service docker start   
    sudo docker run --network host -it -d --entrypoint "java" ghcr.io/legmask/hagimule/diary:latest \
      -Djava.rmi.server.hostname=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4) \
      -cp app.jar n7.HagiMule.Diary.DiaryImpl 0.0.0.0
    
  EOF

  tags = {
    Name = "diary"
  }

  vpc_security_group_ids = [aws_security_group.hagimule-security-group.id]
}

resource "aws_instance" "client" {
  count = var.client_count

  ami                         = "ami-0d16a00c70ee279b8" # Amazon Linux 2 AMI
  instance_type               = "t2.micro"
  associate_public_ip_address = true
  user_data                   = <<-EOF
    #!/bin/bash
    yum update -y
    amazon-linux-extras install docker -y
    service docker start

    mkdir -p /media
    dd if=/dev/zero of=/media/fichier_1go.bin bs=1M count=1024
    dd if=/dev/zero of=/media/fichier_3go.bin bs=1M count=3072
    # dd if=/dev/zero of=/media/fichier_5go.bin bs=1M count=5120

    sudo docker run --network host -d -it --volume /media:/media ghcr.io/legmask/hagimule/client:latest ${aws_instance.diary.public_ip} 4000 --no-tui --files /media/fichier_1go.bin,/media/fichier_3go.bin
  EOF

  tags = {
    Name = "client ${count.index}"
  }

  vpc_security_group_ids = [aws_security_group.hagimule-security-group.id]
}


resource "aws_security_group" "hagimule-security-group" {
  name        = "hagimule-security-group"
  description = "OpenTofu Foundations internet access for EC2 instance"

  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"] # Allow from anywhere (replace with a specific IP range for better security)
  }

  # Needs to be able to get to docker hub to download images
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"] # Allow all outbound traffic
  }
}

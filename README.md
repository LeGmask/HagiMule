# HagiMule

## Benchmark

Results as of 10-01-25 12:54 :
| Clients | Time | Speed    |
| ------- | ---- | -------- |
| 1       | 63   | 16915B/s |
| 2       | 38   | 28032B/s |
| 3       | 21   | 50266B/s |
| 4       | 18   | 58610B/s |


## Launch the project in the cloud

:::info
This project use opentofu to deploy the infrastructure in the cloud.
Every following command should be run in the infra directory.
:::

Before the first run, you need to initialize the project :
```bash
tofu init
```

Next, ensure you have the following environment variables set :
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET`

Then, you can run the following command to deploy the infrastructure in the cloud :

- Single instance of the client and the diary :
```bash
tofu apply -auto-approve
```

- Multiple instances of the client and the diary :
```bash
tofu apply -auto-approve -var 'client_count=5'
```
# HagiMule

## Launch the project in the cloud

- Single instance of the client and the diary :
```bash
tofu apply -auto-approve
```

- Multiple instances of the client and the diary :
```bash
tofu apply -auto-approve -var 'client_count=5'
```
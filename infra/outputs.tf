output "diary_ip" {
  description = "Public IP of the diary"
  value       = aws_instance.diary.public_ip
}

output "clients_ip" {
  description = "Public IP of the client"
  value       = aws_instance.client.*.public_ip
}

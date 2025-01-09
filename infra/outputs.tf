output "instance_url" {
  description = "Public IP of the diary"
  value       = aws_instance.diary.public_ip
}

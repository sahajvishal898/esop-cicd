terraform {


  backend "s3" {
    bucket = "gurukul-vishal-bucket"
    key    = "gurukul.tfstate"
    region = "us-east-1"
  }
}

provider "aws" {
  region  = "us-east-1"
}



data "aws_key_pair" "auth" {
  key_name   = "gurukul-v"
  include_public_key = true
}



resource "aws_subnet" "v_subnet" {
  vpc_id            = "vpc-019c09a1a0c5b4f6b"
  cidr_block        = "10.0.0.16/28"

  tags = {
    Name = "Some Public Subnet"
  }
}


resource "aws_security_group" "v_sg" {
  name   = "HTTP and SSH"
  vpc_id = "vpc-019c09a1a0c5b4f6b"

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = -1
    cidr_blocks = ["0.0.0.0/0"]
  }
}



resource "aws_instance" "web_instance" {
  ami           = "ami-03c1fac8dd915ff60"
  instance_type = "t2.micro"


  subnet_id                   = aws_subnet.v_subnet.id
  vpc_security_group_ids      = [aws_security_group.v_sg.id]

  associate_public_ip_address = true

  key_name = data.aws_key_pair.auth.key_name
  tags = {
    Name = "vishal-gurukul"
  }

  provisioner "local-exec" {
    command = "echo ${self.public_ip} >> private_ips.txt"
  }
}







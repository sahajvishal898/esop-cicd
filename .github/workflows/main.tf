
provider "aws" {
  region  = "us-east-1"
}



resource "aws_s3_bucket" "bucket-v" {
  bucket = "gurukul-vishal-bucket"

  tags = {
    Name = "gurukul-vishal-b"
  }
}






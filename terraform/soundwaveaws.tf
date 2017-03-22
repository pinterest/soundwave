provider "aws" {
  access_key = "<<Your Access Key>>"
  secret_key = "<<Your Secret Key>>"
  region     = "<<Your AWS Region>>"
} 

# Setup the IAM Role for AWS lambda function
resource "aws_iam_role" "lambda_soundwave_role" {
    name = "lambda_soundwave_role"
    assume_role_policy = <<EOF
{
    "Version":"2012-10-17",
    "Statement":{
      "Effect":"Allow",
      "Principal": {"Service":"lambda.amazonaws.com"},
      "Action":"sts:AssumeRole"
    }
}
EOF
}

resource "aws_iam_role_policy" "lambda_soundwave_policy" {
    name = "lambda_soundwave_policy"
    role = "${aws_iam_role.lambda_soundwave_role.id}"
    policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": "arn:aws:logs:*:*:*"
    },
{ 
      "Effect": "Allow",
      "Action": [
        "SQS:SendMessage",
        "SQS:ReceiveMessage",
        "SQS:PurgeQueue",
        "SQS:DeleteMessage",
        "SQS:ChangeMessageVisibility",
        "SQS:GetQueueAttributes",
        "SQS:GetQueueUrl"
      ],
      "Resource": "arn:aws:sqs:us-east-1:093574427811:soundwave-events"
    }
  ]
}
EOF
}

# Setup AWS Lambda function
resource "aws_lambda_function" "sound_wave_notification" {
    filename = "soundwave_lambda.py.zip"
    function_name = "sound_wave_notification"
    role = "${aws_iam_role.lambda_soundwave_role.arn}"
    handler = "soundwave_lambda.lambda_handler"
    runtime = "python2.7"
}
# Setup CloudEvent



# Setup SQS queue
resource "aws_sqs_queue" "soundwave-events" {
  name = "soundwave-events"
  delay_seconds = 0
  max_message_size = 2048
  message_retention_seconds = 345600
  receive_wait_time_seconds = 10
}

resource "aws_sqs_queue_policy" "soundwave" {
  queue_url = "${aws_sqs_queue.soundwave-events.id}"
  policy = <<POLICY
{
  "Version": "2012-10-17",
  "Id": "sqspolicy",
  "Statement": [
    {
      "Sid": "First",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "sqs:SendMessage",
      "Resource": "${aws_sqs_queue.soundwave-events.arn}",
      "Condition": {
        "ArnEquals": {
          "aws:SourceArn": "${aws_sqs_queue.soundwave-events.arn}"
        }
      }
    }
  ]
}
POLICY
}

# Setup the CloudWatch event rule and set target to lambda 
resource "aws_cloudwatch_event_rule" "soundwave_cloudwatch_rule" {
  name = "soundwave_cloudwatch_rule"
  description = "Soundwave Ec2 Notification"
  event_pattern = <<PATTERN
{
  "source": [
    "aws.ec2"
  ],
  "detail-type": [
    "EC2 Instance State-change Notification"
  ],
  "detail": {
    "state": [
      "running",
      "terminated",
      "stopped"
    ]
  }
}
PATTERN
}

resource "aws_cloudwatch_event_target" "lambda" {
  rule = "${aws_cloudwatch_event_rule.soundwave_cloudwatch_rule.name}"
  target_id = "sound_wave_notification"
  arn = "${aws_lambda_function.sound_wave_notification.arn}"
}

 

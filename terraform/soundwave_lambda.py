import json
import boto3

def lambda_handler(event, context):
    # Get the service resource
    sqs = boto3.resource('sqs')
    # Get the queue. This returns an SQS.Queue instance
    queue = sqs.get_queue_by_name(QueueName='soundwave-events')
    # Write to queue
    response = queue.send_message(MessageBody=json.dumps(event))

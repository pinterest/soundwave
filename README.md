# Soundwave


# What is soundwave
Soundwave helps to create a searchable store with UI that keeps all current and historic EC2 instances data with extending schema. It contains three parts:
1. A worker system to sync with EC2 data and push to the local store.
2. An API layer to provide REST APIs to access local search.
3. A dashboard for end users to do ad hoc search

In Pinterest, Soundwave is the core of our CMDB (Configuration Management Database) that serves as the source of truth for our EC2 instances information used by service management and finance purpose.


# Why use soundwave 

Soundwave is useful to EC2 users because:
1. AWS managedment API has a rate limit which makes it not suitable to directly query machine information from applications. 
2. AWS has no information for terminated instances.  
3. In AWS, EC2 schema can only be extended in limited way with tags.

# Getting Started
1. Clone the repositry

```
git clone https://github.com/pinterest/soundwave.git
```

2. Setup AWS Prerequisites:

Soundwave requires some configuration on AWS to receive EC2 instance notifications. All of the required configurations
has been put into a [Terraform](https://www.terraform.io/) file terraform/soundwaveaws.tf. To provison required configrations in AWS, open terraform/soundwaveaws.tf. Input your AWS credential and AWS region into the beginning section of the file:

```
provider "aws" {
  access_key = "<<Your Access Key>>"
  secret_key = "<<Your Secret Key>>"
  region     = "<<Your AWS Region"
} 
```
3. Download [Terraform](https://www.terraform.io/downloads.html) and go to terraform directory. Run
```
terraform apply
```
4. Build the project, go to soundwave root directory and run:
* First, input your region on worker/config/soundwaveworker.properties if it is not us-east-1
```
aws_region=us-east-1
```
* Build the package
```
mvn package
```
6. Open docker-compose.yml, put AWS key id and secret there:

```
version: '2'
services:
  soundwave-worker:
    build: ./worker
    environment:
      - CONFIG_FILE=config/soundwaveworker.properties
      - TEST_CONFIG=1
      - AWS_ACCESS_KEY_ID=<<key_id>>
      - AWS_SECRET_ACCESS_KEY=<<access_key>>
```
7. Run demo with the following command:

```
docker-compose up
```
8. Create ES indexes:

```
worker/provision_index.sh http://localhost:9200/soundwave_prod
worker/provision_index.sh http://localhost:9200/soundwave_ss
```

Try to launch an EC2 instance, you can check the instance created
in the index.

curl http://localhost:9200/soundwave_prod/_search

```
{
   "took": 38,
   "timed_out": false,
   "_shards": {
      "total": 1,
      "successful": 1,
      "failed": 0
   },
   "hits": {
      "total": 1,
      "max_score": 1,
      "hits": [
         {
            "_index": "soundwave_prod",
            "_type": "instance",
            "_id": "i-01c085b645bffcf6f",
            "_score": 1,
            "_source": {
               "id": "i-01c085b645bffcf6f",
               "region": "us-east-1",
               "location": "us-east-1b",
               "state": "running",
               "created_time": "2017-03-21T18:57:56.571+0000",
               "updated_time": "2017-03-21T18:58:27.577+0000",
               "terminated_time": null,
               "vpc_id": "vpc-b58320d0",
               "subnet_id": "subnet-dcfc3cab",
               "aws_launch_time": "2017-03-22T03:49:18.000+0000",
               "security_groups": null,
               "security_group_ids": null,
               "tags": null,
               "cloud": {
                  "aws": {
                     "subnetId": "subnet-dcfc3cab",
                     "kernelId": null,
                     "ramdiskId": null,
                     "virtualizationType": "hvm",
                     "amiLaunchIndex": 0,
                     "enaSupport": true,
                     "stateReason": null,
                     "sourceDestCheck": true,
                     "platform": null,
                     "instanceId": "i-01c085b645bffcf6f",
                     "vpcId": "vpc-b58320d0",
                     "hypervisor": "xen",
                     "rootDeviceName": "/dev/xvda",
                     "state": {
                        "code": 16,
                        "name": "running"
                     },
                     "productCodes": [],
                     "architecture": "x86_64",
                     "ebsOptimized": false,
                     "imageId": "ami-0b33d91d",
                     "blockDeviceMappings": [
                        {
                           "ebs": {
                              "volumeId": "vol-0305a977aad819e7f",
                              "deleteOnTermination": true,
                              "attachTime": 1490154559000,
                              "status": "attached"
                           },
                           "deviceName": "/dev/xvda"
                        }
                     ],
                     "stateTransitionReason": "",
                     "publicIpAddress": "54.242.222.37",
                     "clientToken": "EtnOD1490154557345",
                     "instanceType": "t2.micro",
                     "keyName": "lidaec2key",
                     "publicDnsName": "",
                     "monitoring": "disabled",
                     "iamInstanceProfile": null,
                     "privateIpAddress": "172.30.1.227",
                     "instanceLifecycle": null,
                     "rootDeviceType": "ebs",
                     "tags": [],
                     "launchTime": 1490154558000,
                     "spotInstanceRequestId": null,
                     "networkInterfaces": [
                        {
                           "networkInterfaceId": "eni-0c2febee",
                           "subnetId": "subnet-dcfc3cab",
                           "privateIpAddresses": [
                              {
                                 "association": {
                                    "ipOwnerId": "amazon",
                                    "publicIp": "54.242.222.37",
                                    "publicDnsName": ""
                                 },
                                 "privateDnsName": null,
                                 "privateIpAddress": "172.30.1.227",
                                 "primary": true
                              }
                           ],
                           "description": "Primary network interface",
                           "groups": [
                              {
                                 "groupName": "launch-wizard-8",
                                 "groupId": "sg-7465ce0b"
                              }
                           ],
                           "association": {
                              "ipOwnerId": "amazon",
                              "publicIp": "54.242.222.37",
                              "publicDnsName": ""
                           },
                           "ipv6Addresses": [],
                           "ownerId": "093574427811",
                           "sourceDestCheck": true,
                           "privateIpAddress": "172.30.1.227",
                           "macAddress": "0a:f0:41:07:19:aa",
                           "attachment": {
                              "attachmentId": "eni-attach-3a2f6802",
                              "deleteOnTermination": true,
                              "deviceIndex": 0,
                              "attachTime": 1490154558000,
                              "status": "attached"
                           },
                           "vpcId": "vpc-b58320d0",
                           "privateDnsName": null,
                           "status": "in-use"
                        }
                     ],
                     "sriovNetSupport": null,
                     "privateDnsName": "ip-172-30-1-227.ec2.internal",
                     "securityGroups": [
                        {
                           "groupName": "launch-wizard-8",
                           "groupId": "sg-7465ce0b"
                        }
                     ],
                     "placement": {
                        "availabilityZone": "us-east-1b",
                        "groupName": "",
                        "tenancy": "default",
                        "hostId": null,
                        "affinity": null,
                        "availability_zone": "us-east-1b"
                     }
                  }
               },
               "aws_status": null,
               "token": null,
               "cached": 0
            }
         }
      ]
   }
}
```

# Contacts




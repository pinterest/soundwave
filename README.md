# soundwave



# What is soundwave
Soundwave helps to create a searchable store with UI that keeps all current and historic EC2 instances data with extending schema. It contains three parts:
1. A worker system to sync with EC2 data and push to the local store.
2. An API layer to provide REST APIs to access local search.
3. A dashboard for end users to do ad hoc search

In Pinterest, Soundwave is the core of our CMDB (Configuration Management Database) that serves as the source of truth for our EC2 instances information used by service management and finance purpose.


# Why use soundwave 

Soundwave is useful to EC2 users because:
1. AWS managedment API has a rate limit which makes it not suitable to directly query machine information from applications. 
2. AWS has no information for terminated instances. There is 
3. In AWS, EC2 schema can only be extended in limited way.

# Getting Started


* Prerequisites:

* Onebox Installation:

* Run Worker


* Run API Service

* Run Dashboard


# Contacts




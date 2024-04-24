This file explains all the AWS resoruces need to deploy the application:

##################
### DYNAMO DB ###
##################
attractions-table
	- partition key: id (Number)
	- on-demand

queues-table
	- partition key: attractionId (Number)
	- sort key: userId (Number)
	- on-demand
	- Secondary global index: user-queues-index
		- partition key: userId (Number)
		- sort key: attractionId (Number)
		- attribute projections: all

tickets-table
	- partition key: ticketId (String)
	- on-demand

users-table
	- partition key: userId (Number)
	- on-demand	

#########################
### LAMBDA FUNCTIONS ###
#########################

All lambdas: 
	- Python 3.12 with 30sec timeout
	- All lambdas need to be configured with correct facility queue tables
	- Each lambda file must be called 'lambda_function.py' or else AWS will not recognise it
	- Some lambdas have extra dependecies that must be added manually using a virtual environment and included as part of the .zip upload to AWS
		- add-test-traffic: ulid-py
		- generate-ticket: segeno
		- join-queue: ulid-py
		

check-ticket and generate-ticket
	- Require the endpoints from the API stage to be edited within the function
	- Generate ticket additionally needs the S3 bucket

#############
### ROLES ###
#############

Q-Up-Get-Attractions-Lambda-Role - full Cloudwatch, read DynamoDB access
	- get-attraction-queue
	- get-attractions
	- get-user-queues
	
Q-Up-Post-Attractions-Lambda-Role - full Cloudwatch and DynamoDB access
	- add-test-traffic
	- check-ticket
	- check-user-id-valid
	- complete-queue
	- join-queue
	- leave-queue
	- post-attraction-data
	- remove-test-traffic
	- update-queue-call-num

Q-Up-Generate-Ticket		- full Cloudwatch, S3 and DynamoDB access
	- generate-ticket

###################
### API GATEWAY ###
###################

API Gateway - REST API, Regional
/attractions
	- GET
	- linked to get-attractions

/attractions
	- POST
	- linked to post-attraction-data
	- AWS_IAM auth


/complete-queue
	- GET
	- linked to complete-queue
	- lambda proxy integration enabled
	- params
		- attractionId
		- userId

/join-queue
	- POST
	- linked to join-queue

/join-queue/test-traffic
	- POST
	- linked to join-queue
	- AWS_IAM auth

/leave-queue
	- POST
	- linked to leave-queue

/leave-queue/test-traffic
	- POST
	- linked to leave-queue
	- AWS_IAM auth

/ticket/check
	- GET
	- linked to check-ticket
	- lambda proxy integration enabled
	- params
		- ticketId

/ticket/generate
	- POST
	- linked to generate-ticket
	- AWS_IAM auth

/update-queue
	- PUT
	- linked to update-queue-call-num

/user-id
	- GET
	- linked to check-user-id-valid
	- lambda proxy integration enabled
	- params
		- userId

/user-queues
	- GET
	- linked to get-user-queues
	- lambda proxy integration enabled
	- params
		- userId









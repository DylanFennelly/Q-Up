import json
import boto3
from datetime import datetime, timezone
from botocore.exceptions import ClientError


def lambda_handler(event, context):
    dynamodb = boto3.resource('dynamodb')
    
    tickets_table_name = 'disland-paris-tickets-table'
    users_table_name = 'disland-paris-users-table'
    
    tickets_table = dynamodb.Table(tickets_table_name)
    users_table = dynamodb.Table(users_table_name)
    
    try:
        print("event:", json.dumps(event))
        ticket_id = event['queryStringParameters']['ticketId']
        
        
        ticket_response = tickets_table.get_item(
            Key = {
                'ticketId': ticket_id
            }
        )
        
        start_date = ticket_response['Item']['startDate']
        end_date = ticket_response['Item']['endDate']
        activated = ticket_response['Item']['activated']
        
        date_format = '%Y-%m-%dT%H:%M:%SZ'
        now_utc = datetime.now(timezone.utc)
        start_date_time = datetime.strptime(start_date, date_format).replace(tzinfo=timezone.utc)           # parsing strings to date-time format
        end_date_time = datetime.strptime(end_date, date_format).replace(tzinfo=timezone.utc)
        
        # if ticket hasnt reached starttime yet:
        if now_utc < start_date_time:
            return {
                'statusCode': 460,
                'headers': {
                    'Content-Type': 'application/json'
                },
                'body': json.dumps({
                    'statusCode': 460, 
                    'body': {
                        'message': f"Ticket is not active yet. Active from: {start_date}",
                        'userId': -1,
                        'facilityName': "",
                        'baseUrl': 'https://failed.com/',
                        'mapLat': 0.0,
                        'mapLng': 0.0
                    }
                    
                }),
                "isBase64Encoded": False
            }
        
        # if ticket has expired:
        if now_utc > end_date_time:
            return {
                'statusCode': 461,
                'headers': {
                    'Content-Type': 'application/json'
                },
                'body': json.dumps({
                    'statusCode': 461, 
                    'body': {
                        'message': f"Ticket has expired. Expired at: {end_date}",
                        'userId': -1,
                        'facilityName': "",
                        'baseUrl': 'https://failed.com/',
                        'mapLat': 0.0,
                        'mapLng': 0.0
                    }
                    
                }),
                "isBase64Encoded": False
            }
        
        # if ticket has been activated:    
        if activated:
            return {
                'statusCode': 462,
                'headers': {
                    'Content-Type': 'application/json'
                },
                'body': json.dumps({
                    'statusCode': 462, 
                    'body': {
                        'message': "Ticket has already been activated.",
                        'userId': -1,
                        'facilityName': "",
                        'baseUrl': 'https://failed.com/',
                        'mapLat': 0.0,
                        'mapLng': 0.0
                    }
                }),
                "isBase64Encoded": False
            }
        
        
        # if ticket is valid:
        
        users_response = users_table.scan()
        
        users = users_response['Items']
        
         # Ensure entire table is scanned - https://stackoverflow.com/questions/36780856/complete-scan-of-dynamodb-with-boto3
        while 'LastEvaluatedKey' in users_response:
            response = users_table.scan(ExclusiveStartKey=response['LastEvaluatedKey'])
            users.extend(users_response['Items'])
            
        # get the highest userId in the table
        highest_user_id = max([int(item['userId']) for item in users], default=0)
        # go one higher
        new_user_id = highest_user_id + 1
        now_utc_string = now_utc.strftime(date_format)
        
        new_user = {
            'userId': new_user_id,
            'leasedAt': now_utc_string,
            'expiresAt': end_date,
            'ticketId': ticket_id
        }
        
        # add new user
        try:
            new_user_response = users_table.put_item(
                Item=new_user
            )
            
        except ClientError as e:
            if e.response['Error']['Code'] == 'EntityAlreadyExists':
                return {
                    'statusCode': 463,
                    'headers': {
                        'Content-Type': 'application/json'
                    },
                    'body': json.dumps({
                        'statusCode': 463, 
                        'body': {
                            'message': 'Error allocating UserID. Please try again.',
                            'userId': -1,
                            'facilityName': "",
                            'baseUrl': 'https://failed.com/',
                            'mapLat': 0.0,
                            'mapLng': 0.0
                        }
                        
                    }),
                    "isBase64Encoded": False
                }
            else:
                return {
                    'statusCode': 464,
                    'headers': {
                        'Content-Type': 'application/json'
                    },
                    'body': json.dumps({
                        'statusCode': 464, 
                        'body': {
                            'message': 'Error allocating UserID. Please try again.',
                            'userId': -1,
                            'facilityName': "",
                            'baseUrl': 'https://failed.com/',
                            'mapLat': 0.0,
                            'mapLng': 0.0
                        }
                    }),
                    "isBase64Encoded": False
                }
                
        #update ticket to be activated
        update_response = tickets_table.update_item(
            Key = {
                'ticketId': ticket_id,
            },
            UpdateExpression="set activated = :a",
            ExpressionAttributeValues={
                ':a': True
            },
            ReturnValues="UPDATED_NEW"
        )
        
        response_body = {
            'message': 'UserId leased',
            'userId': new_user_id,
            'facilityName': "Disneyland Paris",
            'baseUrl': 'https://on52nrecc2.execute-api.eu-west-1.amazonaws.com/disland-paris/',
            'mapLat': 48.86990554265008,
            'mapLng': 2.7802249970387014, 
        }
        
        return {
                'statusCode': 200,
                'headers': {
                    'Content-Type': 'application/json'
                },
                'body': json.dumps({'statusCode': 200, 'body': response_body}),
                "isBase64Encoded": False
            }
        
        
    except Exception as e:
        #sending default body to simplify retrofit config
        return {
            'statusCode': 500,
            'headers': {
                'Content-Type': 'application/json'
            },
            'body': json.dumps({
                'statusCode': 500, 
                'body': {
                    'message': f'An error occured: {e}',
                    'userId': -1,
                    'facilityName': "",
                    'baseUrl': 'https://failed.com/',
                    'mapLat': 0.0,
                    'mapLng': 0.0
                }
            }),
            "isBase64Encoded": False
        }

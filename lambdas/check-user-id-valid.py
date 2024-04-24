import json
import boto3
from datetime import datetime, timezone
from botocore.exceptions import ClientError


def lambda_handler(event, context):
    # Checks if user id is still valid (exists and hasnt expired)
    dynamodb = boto3.resource('dynamodb')

    table_name = 'disland-paris-users-table'
    
    table = dynamodb.Table(table_name)
    
    try:
        print("event:", json.dumps(event))
        user_id = int(event['queryStringParameters']['userId'])
        
        
        response = table.get_item(
            Key = {
                'userId': user_id
            }
        )
        
        # id doesnt exist -> no longer valid
        if not response:
            return {
                'statusCode': 200,
                'headers': {
                    'Content-Type': 'application/json'
                },
                'body': json.dumps({
                    'statusCode': 200, 
                    'body': {
                        'valid': False
                    }
                    
                }),
                "isBase64Encoded": False
            }
        
        expire = response['Item']['expiresAt']
        
        date_format = '%Y-%m-%dT%H:%M:%SZ'
        now_utc = datetime.now(timezone.utc)
        expire_date_time = datetime.strptime(expire, date_format).replace(tzinfo=timezone.utc)  # parsing strings to date-time format
        
        # if user id expired:
        if now_utc > expire_date_time:
            return {
                'statusCode': 200,
                'headers': {
                    'Content-Type': 'application/json'
                },
                'body': json.dumps({
                    'statusCode': 200, 
                    'body': {
                        'valid': False
                    }
                    
                }),
                "isBase64Encoded": False
            }
        else:
            return {
                'statusCode': 200,
                'headers': {
                    'Content-Type': 'application/json'
                },
                'body': json.dumps({
                    'statusCode': 200, 
                    'body': {
                        'valid': True
                    }
                    
                }),
                "isBase64Encoded": False
            }
        
        
    except Exception as e:
        return {
            'statusCode': 500,
            'headers': {
                'Content-Type': 'application/json'
            },
            'body': json.dumps({
                'statusCode': 500, 
                'body': {
                    'valid': False
                }
            }),
            "isBase64Encoded": False
        }

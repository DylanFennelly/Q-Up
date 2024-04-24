import boto3
import json
from decimal import Decimal
from json import JSONEncoder

class DecimalEncoder(JSONEncoder):
    # Convert Decimals to Ints -> https://stackoverflow.com/questions/63278737/object-of-type-decimal-is-not-json-serializable
    def default(self, obj):
        if isinstance(obj, Decimal):
            return int(obj)  
        return JSONEncoder.default(self, obj)


def lambda_handler(event, context):
    
    # Query params in API gateway are a nightmare
    # https://stackoverflow.com/questions/31329958/how-to-pass-a-querystring-or-route-parameter-to-aws-lambda-from-amazon-api-gatew
    # https://stackoverflow.com/questions/43708017/aws-lambda-api-gateway-error-malformed-lambda-proxy-response
    
    
    dynamodb = boto3.resource('dynamodb')
    
    table_name = 'disland-paris-queues-table'
    table = dynamodb.Table(table_name)
    
    try:
        print("event:", json.dumps(event))
        # userId is number in schema -> cast to int
        userId = int(event['queryStringParameters']['userId'])
        attractionId = int(event['queryStringParameters']['attractionId'])
        
        #https://stackoverflow.com/questions/35758924/how-do-we-query-on-a-secondary-index-of-dynamodb-using-boto3
        response = table.get_item(
            Key = {
                'attractionId': attractionId,
                'userId': userId,
            },
        )
        
        queue_entry = response['Item']
        callNum = queue_entry['callNum']
        
        # if ticket is not valid
        if callNum == 5:
            response_body = {'message': "Ticket is no longer valid"}
            
            return {
                'statusCode': 409,
                'headers': {
                    'Content-Type': 'application/json'
                },
                'body': json.dumps(response_body),
                "isBase64Encoded": False
            }
            
        
        # Ticket is valid -> remove user fro mqueue    
        response = table.delete_item(
            Key = {
                'attractionId': attractionId,
                'userId': userId,
            },
        )
        
        response_body = {'message': "Ticket accepted. User Queue completed", 'attractionId': attractionId, 'userId': userId}
        
        return {
            'statusCode': 200,
            'headers': {
                'Content-Type': 'application/json'
            },
            'body': json.dumps(response_body),
            "isBase64Encoded": False
        }
        
    except Exception as e:
        response_body = {'message': "Ticket not valid or error occured", 'error': str(e)}
        
        return {
            'statusCode': 500,
            'headers': {
                'Content-Type': 'application/json'
            },
            'body': json.dumps(response_body),
            "isBase64Encoded": False
        }
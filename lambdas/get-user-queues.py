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
        user_id = int(event['queryStringParameters']['userId'])
        
        #https://stackoverflow.com/questions/35758924/how-do-we-query-on-a-secondary-index-of-dynamodb-using-boto3
        response = table.query(
            IndexName='user-queues-index',
            KeyConditionExpression=boto3.dynamodb.conditions.Key('userId').eq(user_id)
        )
        
        user_queues = response['Items']
        
        sorted_queues = sorted(user_queues, key=lambda x:x['attractionId'])
        print("sorted items: ", sorted_queues)
        
        for item in sorted_queues:
            # get number of users ahead of user in queue and append to item
            attractionId = item['attractionId']
            timestamp = item['time']
            
            # filtering by non-key value - https://iamvickyav.medium.com/aws-dynamodb-with-python-boto3-part-3-query-items-from-dynamodb-f99e62a34227
            item_response = table.query(
                ExpressionAttributeValues={
                    ':id': attractionId,
                    ':timestamp': timestamp
                },
                # 'time' is a reserved filter word - https://stackoverflow.com/questions/36698945/scan-function-in-dynamodb-with-reserved-keyword-as-filterexpression-nodejs
                ExpressionAttributeNames={
                    '#timeAttr': 'time'
                },
                KeyConditionExpression='attractionId = :id',
                FilterExpression='#timeAttr < :timestamp'
            )
            item['aheadInQueue'] = len(item_response['Items'])
            
        # returning only attractionIds and aheadInQueue values
        response_body = [{'attractionId': item['attractionId'], 'callNum': item['callNum'], 'aheadInQueue': item['aheadInQueue'], 'lastUpdated': item['lastUpdated']} for item in sorted_queues]
        
        
        return {
            'statusCode': 200,
            'headers': {
                'Content-Type': 'application/json'
            },
            'body': json.dumps(response_body, cls=DecimalEncoder),
            "isBase64Encoded": False
        }
        
    except Exception as e:
        return {
            'statusCode': 500,
            'headers': {
                'Content-Type': 'application/json'
            },
            'body': json.dumps(str(e)),
            "isBase64Encoded": False
        }
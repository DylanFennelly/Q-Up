import json
import boto3

def lambda_handler(event, context):
    dynamodb = boto3.resource('dynamodb')
    table = dynamodb.Table('disland-paris-queues-table')
    
    #input list of attraction IDs from function call
    attraction_ids = event['attractionIds']
    print("attraction_ids: ", attraction_ids)
    #dict of queue lengths
    queues = {}
    
    for attraction_id in attraction_ids:
        response = table.query(
            ExpressionAttributeValues={
                ':id': attraction_id  
            },
            KeyConditionExpression='attractionId = :id'
        )
        
        queues[attraction_id] = len(response['Items'])
    
    print("queues: ", queues)
    
    return {
        'statusCode': 200,
        'body': json.dumps(queues)
    }


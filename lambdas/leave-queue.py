import json
import boto3

def lambda_handler(event, context):
    dynamodb = boto3.resource('dynamodb')
    
    table_name = 'disland-paris-queues-table'
    table = dynamodb.Table(table_name)
    
    try:
        
        #https://stackoverflow.com/questions/70343666/python-boto3-float-types-are-not-supported-use-decimal-types-instead
        event_data = json.loads(json.dumps(event))
        print(event_data)
        
        required_fields= ['attractionId', 'userId']
        
        #ensuring body matches format
        for field in required_fields:
            if field not in event_data:
                return {
                    'statusCode': 400,
                    'body': json.dumps(f"Missing required field: {field}")
                }
        
        attractionId = event_data['attractionId']
        userId = event_data['userId']

        response = table.delete_item(
            Key = {
                'attractionId': attractionId,
                'userId': userId,
            },
        )

        return {
            'statusCode': 200,
            'body': 'User removed from queue'
        }
    
    except Exception as e:
        return {
            'statusCode': 500,
            'body': str(e)
        }
        

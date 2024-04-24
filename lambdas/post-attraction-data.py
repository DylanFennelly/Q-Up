import json
import boto3
from decimal import Decimal

def lambda_handler(event, context):
    dynamodb = boto3.resource('dynamodb')
    
    table_name = 'disland-paris-attractions-table'
    table = dynamodb.Table(table_name)
    
    try:
        
        #https://stackoverflow.com/questions/70343666/python-boto3-float-types-are-not-supported-use-decimal-types-instead
        event_data = json.loads(json.dumps(event), parse_float=Decimal)
        print(event_data)
        
        required_fields= ['id', 'name', 'description', 'type', 'status', 'cost', 'length', 'lat', 'lng']
        
        #ensuring body matches format
        for field in required_fields:
            if field not in event_data:
                return {
                    'statusCode': 400,
                    'body': json.dumps(f"Missing required field: {field}")
                }
        
        table.put_item(Item=event_data)
        
        return {
            'statusCode': 200,
            'body': event_data
        }
    except Exception as e:
        return {
            'statusCode': 500,
            'body': str(e)
        }
        
        
## https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.ConditionExpressions.html

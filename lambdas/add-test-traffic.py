import json
import boto3
import ulid
from datetime import datetime, timezone

def lambda_handler(event, context):
    dynamodb = boto3.resource('dynamodb')
    
    table_name = 'disland-paris-queues-table'
    table = dynamodb.Table(table_name)
    
    try:
        
        #https://stackoverflow.com/questions/70343666/python-boto3-float-types-are-not-supported-use-decimal-types-instead
        event_data = json.loads(json.dumps(event))
        print(event_data)
        
        required_fields= ['attractionId', 'startRange', 'endRange']
        
        #ensuring body matches format
        for field in required_fields:
            if field not in event_data:
                return {
                    'statusCode': 400,
                    'body': json.dumps(f"Missing required field: {field}")
                }
        
        attractionId = event_data['attractionId']
        start_range = event_data['startRange']
        end_range = event_data['endRange']
       
        id_range = range(start_range, end_range+1)

        for id in id_range:
            time = str(ulid.new())      #time stamp - ULID   https://pypi.org/project/ulid-py/
            callNum = 0
            now_utc = datetime.now(timezone.utc)        # getting a timestamp to use as last update time (for making calls after entrance ticket is generated)
            lastUpdated = now_utc.strftime('%Y-%m-%dT%H:%M:%SZ')    # matching to format of Instant.now() in Kotlin - https://www.tutorialspoint.com/How-to-get-formatted-date-and-time-in-Python

            item_body = {
                'attractionId': attractionId,
                'userId': id,
                'time': time,
                'callNum': callNum,
                'lastUpdated': lastUpdated
            }
            
            table.put_item(
                Item=item_body,
                ConditionExpression='attribute_not_exists(attractionId) AND attribute_not_exists(userId)'   #check if user has already joined queue for attraction - https://www.cloudtechsimplified.com/check-existing-items-or-duplicates-prevent-overwrites-when-adding-record-in-dynamodb/
                )

        return {
            'statusCode': 200,
            'body': 'Ids in range in queue'
        }
    
    #https://stackoverflow.com/questions/61825481/dynamodb-python-api-way-to-check-result-of-conditional-expression
    except dynamodb.meta.client.exceptions.ConditionalCheckFailedException:
        return {
            'statusCode': 409,  #409 - conflict
            'body': 'User has already queued for this attraction'
        }
    except Exception as e:
        return {
            'statusCode': 500,
            'body': str(e)
        }
        
        
## https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.ConditionExpressions.html

import boto3
import json

def lambda_handler(event, context):
    dynamodb = boto3.resource('dynamodb')
    lambda_client = boto3.client('lambda')
    
    table_name = 'disland-paris-attractions-table'
    table = dynamodb.Table(table_name)
    
    #arn of get-attraction-queue lambda - for internal lambda call
    get_queue_arn = 'arn:aws:lambda:eu-west-1:057140894471:function:disland-paris-get-attraction-queue'
    
    try:
        response = table.scan()
        
        items = response['Items']
        print("Response from DynamoDB: ", items)
        
        # Ensure entire table is scanned - https://stackoverflow.com/questions/36780856/complete-scan-of-dynamodb-with-boto3
        while 'LastEvaluatedKey' in response:
            response = table.scan(ExclusiveStartKey=response['LastEvaluatedKey'])
            items.extend(response['Items'])
            
        #https://www.freecodecamp.org/news/sort-dictionary-by-value-in-python/
        sorted_items = sorted(items, key=lambda x:x['id'])
        print("sorted items: ", sorted_items)
        
        # get Ids for get_queues function
        # one function call instead of exponentially more
        attraction_ids = [int(item['id']) for item in sorted_items]
        print("attraction_ids: ", attraction_ids)
        
        # calling lambda inside lambda - https://www.sqlshack.com/calling-an-aws-lambda-function-from-another-lambda-function/
        queue_response = lambda_client.invoke(
            FunctionName=get_queue_arn,
            InvocationType='RequestResponse',
            Payload=json.dumps({'attractionIds': attraction_ids})
        )
        
        #retreiving API response from get queues function
        queues_payload = json.loads(queue_response['Payload'].read().decode('utf-8'))
        print("queues_payload: ", queues_payload)
        
        #extracing dict from payload body
        queues = json.loads(queues_payload['body'])
        print("queues body: ", queues)
        
        # appending queue length to each item
        # convert id to str to match string keys of dict, 0 as fallback if 
        for item in sorted_items:
            item['in_queue'] = queues.get(str(item['id']))
            
        print("items with append queuse: ", sorted_items)
        
        return {
            'statusCode': 200,
            'body': sorted_items
        }
    except Exception as e:
        return {
            'statusCode': 500,
            'body': str(e)
        }
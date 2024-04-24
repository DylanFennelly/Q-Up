import json
import boto3
import uuid
from datetime import datetime, timezone, timedelta
import segno
from io import BytesIO


def lambda_handler(event, context):
    dynamodb = boto3.resource('dynamodb')
    s3 = boto3.client('s3')
    
    table_name = 'disland-paris-tickets-table'
    table = dynamodb.Table(table_name)
    
    try:
        event_data = json.loads(json.dumps(event))
        print(event_data)
        
        # Date must match format: '%Y-%m-%dT%H:%M:%SZ'
        # e.g.: 2024-04-22T13:35:57Z
        # Input date should be in UTC
        date_format = '%Y-%m-%dT%H:%M:%SZ'
        
        # if start date specified, use it. Else, set start date to now UTC.
        if 'startDate' in event_data:
            start_date = event_data['startDate']
        else:
            now_utc = datetime.now(timezone.utc)       
            start_date = now_utc.strftime(date_format)
            
        # if end date specified, use it. Else, set end date to expire 15 hours after
        if 'endDate' in event_data:
            end_date = event_data['endDate']
        else:
            start_date_time = datetime.strptime(start_date, date_format).replace(tzinfo=timezone.utc)       ##parsing from string to datetime for time calculation
            end_date_time = start_date_time + timedelta(hours=15)
            end_date = end_date_time.strftime(date_format)       #parsing back to string
            
            
        ticket_id = uuid.uuid4()
        ticket_id_str = str(ticket_id)
        
        response_body = {'message': "Ticket created", 'startDate': start_date, 'endDate': end_date, 'ticketId': ticket_id_str, 'ticket_path': f'DislandParis/{ticket_id_str}.png'}
        
        table.put_item(
            Item={
                'ticketId': ticket_id_str,
                'startDate': start_date,
                'endDate': end_date,
                'activated': False
            }
        )


        #https://realpython.com/python-generate-qr-code/
        qrcode = segno.make_qr(f'https://on52nrecc2.execute-api.eu-west-1.amazonaws.com/disland-paris/ticket/check?ticketId={ticket_id_str}')

        buffer = BytesIO()
        qrcode.save(buffer, kind='png', scale=12)
        buffer.seek(0)

        s3.upload_fileobj(buffer, 'q-up-bucket', f'DislandParis/{ticket_id_str}.png')

        return {
            'statusCode': 200,
            'body': response_body
        }
    
    except Exception as e:
        return {
            'statusCode': 500,
            'body': str(e)
        }

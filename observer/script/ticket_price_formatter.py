def to_ticket_price_message(message):
    return message.replace('n', '원/').replace('.', '장 ') + '장'

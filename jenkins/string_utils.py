def truncate_message(message, max_lines):
    error_message_lines = message.splitlines()
    if len(error_message_lines) > max_lines:
        message = "\n".join(error_message_lines[:max_lines]) + "\n... truncated"
    return message

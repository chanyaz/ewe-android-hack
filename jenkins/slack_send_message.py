from slackclient import SlackClient
from string_utils import truncate_message

def send_private_slack_message(access_token, user_email_address, message):
    sc = SlackClient(access_token)
    channel_id = open_im_channel_to_user_with_email(sc, user_email_address)
    message = truncate_message(message, 40)
    if channel_id != "":
        sc.api_call("chat.postMessage",
                    as_user="true",
                    channel=channel_id,
                    text=message)

def send_public_slack_message(access_token, channel_name, message):
    sc = SlackClient(access_token)
    sc.api_call("chat.postMessage",
                as_user="false",
                username="mergebot",
                icon_url="https://avatars0.githubusercontent.com/u/12072994",
                channel=channel_name,
                text=message)


def open_im_channel_to_user_with_email(slack_client, user_email_address):
    user_id = get_user_id_for_email_address(slack_client, user_email_address)
    if user_id != "":
        im_session = slack_client.api_call("im.open", user=user_id)
        return im_session["channel"]["id"]
    return ""

def get_user_id_for_email_address(slack_client, user_email_address):
    all_users = slack_client.api_call("users.list")
    for user in all_users["members"]:
        profile = user["profile"]
        if "email" in profile:
            if profile["email"] == user_email_address:
                return user["id"]
    return ""

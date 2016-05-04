import base64
import datetime
import dateutil.tz
import hmac
import hashlib
from requests.auth import AuthBase
from urlparse import urlparse
from urllib import urlencode

class HMACAuth(AuthBase):
	def __init__(self, api_key, secret_key):
		self.api_key = api_key
		self.secret_key = secret_key

	def __call__(self, r):
		method = r.method.upper()

		content_type = r.headers.get('Content-Type')
		if not content_type:
			content_type = ''

		content_md5  = r.headers.get('Content-Md5')
		if not content_md5:
			content_md5 = ''

		httpdate = r.headers.get('Date')
		if not httpdate:
			now = datetime.datetime.utcnow()
			httpdate = now.strftime('%a, %d %b %Y %H:%M:%S GMT')
			r.headers['Date'] = httpdate

		url  = urlparse(r.url)
		path = url.path
		if url.query:
			path = path + '?' + url.query

		string_to_sign = '%s,%s,%s,%s' % (content_type, content_md5, path, httpdate)
		digest = hmac.new(self.secret_key, string_to_sign, hashlib.sha1).digest()
		signature = base64.encodestring(digest).rstrip()

		r.headers['Authorization'] = 'APIAuth %s:%s' % (self.api_key, signature)
		return r

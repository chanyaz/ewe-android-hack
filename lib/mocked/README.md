mocke3
======

Dead simple mock response server. Easy to see where the responses are coming from.

Running Locally
===============

Must run from the root of the `ewe-android-eb` repository.

    $ ./gradlew :lib:mocked:mocke3:run

Template Formatting
===================

JSON templates are formatted using `python -m json.tool`

    $ cat unformatted.json | python -m json.tool > formatted.json
    $ # OR from clipboard
    $ pbpaste | python -m json.tool > formatted.json


Expedia Bookings Android
========================

- Change `API` to `CUSTOM_SERVER`
- Force custom server to use http (the mock server does not handle https)
- Under the development settings, enter `Server/Proxy` address as the `ip:port` of your locally running `mocke3` server.
  - If you have an address assigned by the local network you can use that
  - Otherwise you need this for [Genymotion](http://bbowden.tumblr.com/post/58650831283/accessing-a-localhost-server-from-the-genymotion).

Other
=====

Post-Man is a useful chrome extension for running HTTP requests.

https://chrome.google.com/webstore/detail/postman-rest-client/fdmmgilgnpjigdojojpjoooidkmcomcm?hl=en

TODO
====

- [X] Hotel Happy Path
- [X] One Way Flight Happy Path
- [X] Round Trip Flight Happy Path
- [X] Expedia Suggestion Service (ESS) responses
- [ ] Date agnostic flight responses
- [ ] Itinerary responses
- [ ] Sessions for logged in user testing

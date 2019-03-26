# JavaNEST
The JavaNEST Class can be used to interface with the devices on a NEST account through the [NEST Cloud API](https://developers.nest.com/reference/api-overview)

In order to use JavaNEST, you must register your application with the [NEST Developer Console](https://console.developers.nest.com) and obtain your Client ID, Client Secret, and Authorization URL.  The Authorization URL will be used to generate a PIN for each user to authenticate requests.

Right now the codebase is set up only to interact with a NEST Thermostat, however it should be simple to extend to other NEST devices.

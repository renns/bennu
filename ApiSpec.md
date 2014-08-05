## Generic Notes

* Pass the channel id over the HTTP header 'Qoid-ChannelId'

## Agent

### Create Agent

**Submit over channel:** No

**Path:** */api/v1/agent/create*

**Parameters:**

* name: String
* password: String

**Response Parameters:**

* authenticationId: String

**Error Codes:**

* nameInvalid
* nameDuplicate
* passwordInvalid

## Session

### Login

**Submit over channel:** No

**Path:** */api/v1/login*

**Parameters:**

* authenticationId: String
* password: String

**Response Parameters:**

* channelId: String
* connectionIid: String - The logged in alias's connectionId.  To get the alias object, you must query for it, using the connctionIid.

**Error Codes:**

* authenticationFailed

### Logout

**Submit over channel:** No

**Path:** */api/v1/logout*

**Parameters:** None

**Response Parameters:** None

**Error Codes:** None

**Notes:**

* Do not send using the submit channel requests server
* Before calling logout, disable any future polling on the channel

## Channel

### Poll Channel

**Submit over channel:** No

**Path:** */api/v1/channel/poll*

**Parameters:**

* timeoutMillis: Integer
* byteCount: Integer (optional)

**Response Parameters:**

* JSON (Array of responses)

**Error Codes:**

* timeoutMillisInvalid
* byteCountInvalid

**Notes:**

* The timeoutMillis parameter must be passed as a URL parameter (e.g. */api/v1/channel/poll/5000*)

**Questions:**

* Why does the request body dissapear when the continuation completes?
* Do we need byteCount or need to handle it better?

### Submit Channel Requests

**Submit over channel:** No

**Path:** */api/v1/channel/submit*

**Parameters:**

* requests: Array of requests

**Response Parameters:** None

**Error Codes:** None

## Query

### Query

**Submit over channel:** Yes

**Path:** */api/v1/query*

**Parameters:**

* route: Array of Strings
* type: String
* query: String
* historical: Boolean
* standing: Boolean

**Response Parameters:**

* standing: Boolean
* type: String
* results: Array of JSON
* action: String (optional)

**Error Codes:**

* routeInvalid
* typeInvalid
* queryInvalid
* historicalStandingInvalid

**Notes:**

* At least one of *historical* or *standing* must be provided
* The context passed in submit channel request is used to identify the query and the responses
* Standing queries aren't working yet

### Cancel Query

*Not yet implemented*

## Alias

### Create Alias

**Submit over channel:** Yes

**Path:** */api/v1/alias/create*

**Parameters:**

* route: Array of Strings
* name: String
* profileName: String
* profileImage: String (optional)
* data: JSON (optional)

**Response Parameters:**

* alias fields

**Error Codes:**

* routeInvalid
* nameInvalid
* profileNameInvalid

### Update Alias

**Submit over channel:** Yes

**Path:** */api/v1/alias/update*

**Parameters:**

* route: Array of Strings
* aliasIid: String
* data: JSON

**Response Parameters:**

* alias fields

**Error Codes:**

* routeInvalid

### Delete Alias

**Submit over channel:** Yes

**Path:** */api/v1/alias/delete*

**Parameters:**

* route: Array of Strings
* aliasIid: String

**Response Parameters:**

* aliasIid: String

**Error Codes:**

* routeInvalid

**Notes:**

* Currently not implemented
* Need to figure out what will actually be deleted

### Create Alias Login

**Submit over channel:** Yes

**Path:** */api/v1/alias/login/create*

**Parameters:**

* route: Array of Strings
* aliasIid: String
* password: String

**Response Parameters:**

* login fields

**Error Codes:**

* routeInvalid
* passwordInvalid

### Update Alias Login

**Submit over channel:** Yes

**Path:** */api/v1/alias/login/update*

**Parameters:**

* route: Array of Strings
* aliasIid: String
* password: String

**Response Parameters:**

* login fields

**Error Codes:**

* routeInvalid
* passwordInvalid

### Delete Alias Login

**Submit over channel:** Yes

**Path:** */api/v1/alias/login/delete*

**Parameters:**

* route: Array of Strings
* aliasIid: String

**Response Parameters:**

* aliasIid: String

**Error Codes:**

* routeInvalid

### Update Alias Profile

**Submit over channel:** Yes

**Path:** */api/v1/alias/profile/update*

**Parameters:**

* route: Array of Strings
* aliasIid: String
* profileName: String (optional)
* profileImage: String (optional)

**Response Parameters:**

* profile fields

**Error Codes:**

* routeInvalid
* profileNameProfileImageInvalid

**Notes:**

* At least one of *profileName* or *profileImage* must be provided

## Connection

### Delete Connection

**Submit over channel:** Yes

**Path:** */api/v1/connection/delete*

**Parameters:**

* route: Array of Strings
* connectionIid: String

**Response Parameters:**

* connectionIid: String

**Error Codes:**

* routeInvalid

## Content

### Create Content

**Submit over channel:** Yes

**Path:** */api/v1/content/create*

**Parameters:**

* route: Array of Strings
* contentType: String
* data: JSON
* labelIids: Array of Strings

**Response Parameters:**

* content fields

**Error Codes:**

* routeInvalid
* contentTypeInvalid
* dataInvalid
* labelIidsInvalid

### Update Content

*Not yet implemented*

### Add Content Label

*Not yet implemented*

### Remove Content Label

*Not yet implemented*

## Label

### Create Label

**Submit over channel:** Yes

**Path:** */api/v1/label/create*

**Parameters:**

* route: Array of Strings
* parentLabelIid: String
* name: String
* data: JSON

**Response Parameters:**

* label fields

**Error Codes:**

* routeInvalid
* nameInvalid

### Update Label

*Not yet implemented*

### Move Label

*Not yet implemented*

### Copy Label

*Not yet implemented*

### Remove Label

*Not yet implemented*

## Notification

### Consume Notification

*Not yet implemented*

## Introduction

### Initiate Introduction

*Not yet implemented*

### Respond to Introduction

*Not yet implemented*

## Verification

### Request Verification

*Not yet implemented*

### Respond to Verification Request

*Not yet implemented*

### Accept Verification

*Not yet implemented*

### Verify

*Not yet implemented*

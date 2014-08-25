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
* connectionIid: String - The logged in alias' connectionId.  To get the alias object, you must query for it, using the connctionIid.

**Error Codes:**

* authenticationFailed

### Logout

**Submit over channel:** No

**Path:** */api/v1/logout*

**Parameters:** None

**Response Parameters:** None

**Error Codes:** None

**Notes:**

* Even though logout should not be called using the submit channel requests service, it still needs the ChannelId HTTP header value
* Do not send using the submit channel requests server
* Before calling logout, disable any future polling on the channel

### Spawn Session

**Submit over channel:** Either

**Path:** */api/v1/session/spawn*

**Parameters:**

* aliasIid: String

**Response Parameters:**

* channelId: String
* connectionIid: String - The logged in alias's connectionId.  To get the alias object, you must query for it, using the connctionIid.

**Error Codes:** None

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
* profileNameInvalid
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

**Submit over channel:** Yes

**Path:** */api/v1/content/update*

**Parameters:**

* route: Array of Strings
* contentIid: InternalId
* data: JSON

**Response Parameters:**

* content fields

**Error Codes:**

* routeInvalid
* dataInvalid

### Add Content Label

**Submit over channel:** Yes

**Path:** */api/v1/content/label/add*

**Parameters:**

* route: Array of Strings
* contentIid: String
* labelIid: String

**Response Parameters:**

* contentIid: String
* labelIid: String

**Error Codes:**

* routeInvalid
* contentAlreadyHasLabel

### Remove Content Label

**Submit over channel:** Yes

**Path:** */api/v1/content/label/remove*

**Parameters:**

* route: Array of Strings
* contentIid: String
* labelIid: String

**Response Parameters:**

* contentIid: String
* labelIid: String

**Error Codes:**

* routeInvalid
* contentDoesNotHaveLabel

**Notes:**

* If the last label is removed from a piece of content, that content is orphaned and is no longer accessible

## Label

### Create Label

**Submit over channel:** Yes

**Path:** */api/v1/label/create*

**Parameters:**

* route: Array of Strings
* parentLabelIid: String
* name: String
* data: JSON (optional)

**Response Parameters:**

* label fields

**Error Codes:**

* routeInvalid
* nameInvalid

### Update Label

**Submit over channel:** Yes

**Path:** */api/v1/label/update*

**Parameters:**

* route: Array of Strings
* labelId: String
* name: String (optional)
* data: JSON (optional)

**Response Parameters:**

* label fields

**Error Codes:**

* routeInvalid
* nameInvalid
* nameDataInvalid

**Notes:**

* At least one of *name* or *data* must be provided

### Move Label

**Submit over channel:** Yes

**Path:** */api/v1/label/move*

**Parameters:**

* route: Array of Strings
* labelId: String
* oldParentLabelIid: String,
* newParentLabelIid: String

**Response Parameters:**

* labelIid: String

**Error Codes:**

* routeInvalid

### Copy Label

**Submit over channel:** Yes

**Path:** */api/v1/label/copy*

**Parameters:**

* route: Array of Strings
* labelId: String
* newParentLabelIid: String

**Response Parameters:**

* labelIid: String

**Error Codes:**

* routeInvalid

### Remove Label

**Submit over channel:** Yes

**Path:** */api/v1/label/remove*

**Parameters:**

* route: Array of Strings
* labelId: String
* parentLabelIid: String

**Response Parameters:**

* labelIid: String

**Error Codes:**

* routeInvalid

### Grant Label Access

**Submit over channel:** Yes

**Path:** */api/v1/label/access/grant*

**Parameters:**

* route: Array of Strings
* labelId: String
* connectionIid: String
* maxDoV: Integer

**Response Parameters:**

* labelIid: String

**Error Codes:**

* routeInvalid
* maxDoVInvalid
* connectionAlreadyHasAccess

**Notes:**

* *maxDoV* must be greater than 0

### Revoke Label Access

**Submit over channel:** Yes

**Path:** */api/v1/label/access/revoke*

**Parameters:**

* route: Array of Strings
* labelId: String
* connectionIid: String

**Response Parameters:**

* labelIid: String

**Error Codes:**

* routeInvalid
* connectionDoesNotHaveAccess

### Update Label Access

**Submit over channel:** Yes

**Path:** */api/v1/label/access/update*

**Parameters:**

* route: Array of Strings
* labelId: String
* connectionIid: String
* maxDoV: Integer

**Response Parameters:**

* labelIid: String

**Error Codes:**

* routeInvalid
* maxDoVInvalid
* connectionDoesNotHaveAccess

**Notes:**

* *maxDoV* must be greater than 0

## Notification

### Consume Notification

**Submit over channel:** Yes

**Path:** */api/v1/notification/consume*

**Parameters:**

* route: Array of Strings
* notificationIid: String

**Response Parameters:**

* notificationIid: String

**Error Codes:**

* routeInvalid

## Introduction

### Initiate Introduction

**Submit over channel:** Yes

**Path:** */api/v1/introduction/initiate*

**Parameters:**

* route: Array of Strings
* aConnectionIid: String
* aMessage: String
* bConnectionIid: String
* bMessage: String

**Response Parameters:**

* introductionIid: String

**Error Codes:**

* routeInvalid
* aMessageInvalid
* bMessageInvalid

### Accept Introduction

**Submit over channel:** Yes

**Path:** */api/v1/introduction/accept*

**Parameters:**

* route: Array of Strings
* notificationIid: String

**Response Parameters:**

* notificationIid: String

**Error Codes:**

* routeInvalid

## Verification

### Request Verification

*Not yet implemented*

### Respond to Verification Request

*Not yet implemented*

### Accept Verification

*Not yet implemented*

### Verify

*Not yet implemented*

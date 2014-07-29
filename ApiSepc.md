## Agent

### Create Agent

**Submit over channel:** No

**Path:** */api/v1/agent/create*

**Parameters:**

* name: String
* password: String

**Response Parameters:**

* authenticationId: String

## Session

### Login

**Submit over channel:** No

**Path:** */api/v1/login*

**Parameters:**

* authenticationId: String
* password: String

**Response Parameters:**

* channelId: String
* connectionIid: String

### Logout

**Submit over channel:** No

**Path:** */api/v1/logout*

**Parameters:** None

**Response Parameters:** None

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

**Notes:**

* The timeoutMillis parameter must be passed as a URL parameter (e.g. */api/v1/channel/poll/5000*)

### Submit Channel Requests

**Submit over channel:** No

**Path:** */api/v1/channel/submit*

**Parameters:**

* requests: Array of requests

**Response Parameters:** None

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

**Result Kinds**: QueryResponse, StandingQueryResponse

**QueryResponse Parameters:**

* type: String
* results: Array of JSON

**StandingQueryResponse Parameters:**

* type: String
* result: JSON
* action: String

**Notes:**

* The context passed in submit channel request is used to identify the query and the responses
* Standing queries aren't working yet

### Cancel Query

*Not yet implemented*

## Alias

### Create Alias

*Not yet implemented*

### Update Alias

*Not yet implemented*

### Create Alias Login

*Not yet implemented*

### Delete Alias Login

*Not yet implemented*

### Update Alias Login

*Not yet implemented*

### Update Alias Profile

*Not yet implemented*

## Connection

### Delete Connection

*Not yet implemented*

## Content

### Create Content

**Submit over channel:** Yes

**Path:** */api/v1/content/create*

**Parameters:**

* route: Array of Strings
* contentType: String
* data: JSON
* labelIids: Array of Strings

**Result Kind**: CreateContentResponse

**CreateContentResponse Parameters:**

* content: JSON

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
* data: AJSON

**Result Kind**: CreateLabelResponse

**CreateLabelResponse Parameters:**

* label: JSON

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

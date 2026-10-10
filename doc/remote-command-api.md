# QImgExtractor Remote Command API

QImgExtractor is a desktop (Swing) application for cutting question images out
of scanned exam pages. It exposes a small HTTP API so that other programs can
drive its UI. This document describes that API for callers.

## Connection

| Item     | Value                                                           |
|----------|-----------------------------------------------------------------|
| Base URL | `http://localhost:8082` (or `http://127.0.0.1:8082`)             |
| Port     | `8082` (set by `server.port` in QImgExtractor's `application.properties`) |
| Access   | Same machine only. The server binds to `127.0.0.1` (`server.address`), so it cannot be reached from other machines |
| Auth     | None                                                            |
| Format   | Request parameters in the query string; responses are JSON      |

QImgExtractor must already be running. If the port refuses connections, the
application is not running; callers cannot start it through this API.

---

## Show a question image

Brings a question image into view in QImgExtractor's UI.

```
POST /api/remote/show-question-image?imgName=<question image name>
```

On receiving a valid request, QImgExtractor:

1. Brings its window to the front (restoring it if minimised).
2. Closes the currently open project and opens the project the image belongs
   to. If that project is already open, it is not reloaded.
3. Switches to the image-cutting view.
4. Selects the tab for the page that contains the image, opening the tab if
   it was closed.
5. Scrolls the page so the image's region is visible. If the region is
   already fully visible, no scrolling happens.

### Parameter

| Name      | Required | Description |
|-----------|----------|-------------|
| `imgName` | yes      | The question image file name, of the form `<projectName>.<pageNumber>.<tag>.png`. The `.png` extension is optional. A full path is accepted; only the file name part is used. Remember to URL-encode the value (tags may contain `(` and `)`). |

### Question image name format

```
<projectName>.<pageNumber>.<subjectCode>_<question id parts>[(<partNumber>)].png
```

- `projectName` – name of the project directory, e.g. `RB-PM-P-01`,
  `AITS-13-A-FT1P1`, `MN-P-...`. QImgExtractor searches its configured source
  base directory (recursively) for a project directory with exactly this name.
- `pageNumber` – page number, normally zero-padded to 3 digits, e.g. `003`.
- `tag` – everything after the page number: subject code (`P`, `C` or `M`),
  question type (`SCA`, `NVT`, `MCA`, `LCT`, `IVT`, `MMT`, `ART`) and question
  number, plus an optional part number in parentheses.

Examples:

```
RB-PM-P-01.003.P_SCA_12.png
AITS-13-A-FT1P1.007.C_MCA_34(2).png
```

The name must be the exact file name of an image in the project's
`question-images/` folder. Callers should pass names they obtained from
QImgExtractor or SConsole as-is rather than constructing them.

### Example

```bash
curl -X POST \
  "http://localhost:8082/api/remote/show-question-image?imgName=RB-PM-P-01.003.P_SCA_12.png"
```

### Responses

The response is sent **before** the UI finishes its work. Opening a project
can take several seconds and may stop at a confirmation dialog that a person
has to answer, so the API does not wait for it.

**202 Accepted** – the request is valid and has been handed to the UI.

```json
{
  "status": "accepted",
  "projectDir": "/Users/sandeep/Documents/StudyNotes/question-bank/RB-PM-P/RB-PM-P-01",
  "pageNumber": 3,
  "imgName": "RB-PM-P-01.003.P_SCA_12.png"
}
```

A `202` does not guarantee the image ends up on screen. Cases where it may not:

- Opening the project shows a confirmation dialog and the user aborts it.
  The project is then not opened (and the previously open project stays
  closed).
- The image file exists but is not registered in the project's page data.
  QImgExtractor then shows "Question image not found" in its status bar and
  nothing else happens.

**400 Bad Request** – `imgName` is not of the expected format (fewer than
three dot-separated parts plus extension, or the page number is not a number).

```json
{
  "status": "error",
  "message": "Image name 'foo.png' is not of the format <srcId>.<pageNum>.<tagName>.png"
}
```

**404 Not Found** – no project directory with that name exists under the
source base directory, or the project has no such file in `question-images/`.

```json
{
  "status": "error",
  "message": "Project 'RB-PM-P-99' not found under /Users/sandeep/Documents/StudyNotes/question-bank"
}
```

A missing `imgName` parameter produces Spring's standard `400` error body
rather than the format above.

### Guidance for callers

- Treat the call as fire-and-forget. Do not retry on `202`; a repeat request
  only re-selects and re-scrolls.
- On `404`, check the image name for typos or a wrong project prefix before
  retrying. Retrying the same name will give the same result.
- Avoid sending bursts of requests for different projects. Each switches
  projects and the user will see the UI jump. If several requests arrive
  while a project is loading, only the last one for that project is shown.
- Switching projects closes whatever the user was working on. Only call this
  API when the user has asked to see the image.

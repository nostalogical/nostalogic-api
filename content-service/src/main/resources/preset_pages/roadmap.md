This roadmap describes some of the intended features for both the Nostalogic API (`nostalogic.io`) and this web client (`nostalogic.net`). While the web client is the primary user of the API, the API has been designed to be potentially very extendable and could be used to support additional projects beyond the main website, so these are considered separate projects here.

## API
##### CMS
The API has the basics of a content management service, or CMS, in place already - all the text on this page is being pulled from a database through the API. The ability to add or update page content is still missing though, so these must be added to complete the content service.
##### Documentation
A swagger based documentation system was previously in place before a complete rewrite of the API was done, but in this latest version this has been postponed until after the launch was completed.
##### File service
The ability to upload images and more generic files. This will be restricted to logged in users, along with some other limitations to prevent abuse.


## Web client
##### CMS
As with the API the client is also missing full CMS capabilities. Markdown has been chosen as the format for most content pages, and while plenty of parsers exist to allow the Angular client to convert that raw data to formatted **text** ~~like~~ `this`, there don't seem to be many options for a prebuilt markdown editor for writing or editing content, so this will need to be built from scratch.

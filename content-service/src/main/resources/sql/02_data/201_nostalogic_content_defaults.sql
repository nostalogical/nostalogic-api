
-- INSERT INTO navigation (id, urn, full_urn, text, icon, ordinal, status)
-- VALUES ('0ed05608-701b-456c-ae12-50009e3fe9a9', '', '', 'Home', 'test', 0, 'ACTIVE') ON CONFLICT DO NOTHING;
--
-- INSERT INTO container(id, navigation_id, "type", resource_id, locale)
-- VALUES ('46c827e6-0d01-4f74-842f-d2c373a0ad9a', '0ed05608-701b-456c-ae12-50009e3fe9a9', 'ARTICLE', '0e36d9f1-3060-4def-925c-18bf985db25b', 'en_GB') ON CONFLICT DO NOTHING;
--
-- INSERT INTO article(id, name, contents, status)
-- VALUES ('0e36d9f1-3060-4def-925c-18bf985db25b', 'Main page', '### Welcome!
-- This is the main page of Nostalogic.', 'ACTIVE') ON CONFLICT DO NOTHING;
--
-- INSERT INTO navigation (id, urn, full_urn, text, icon, ordinal, status)
-- VALUES ('d7318720-e807-409a-99c5-2ae2e7817289', 'test', 'test', 'Test Page 1', 'test', 0, 'ACTIVE') ON CONFLICT DO NOTHING;
--
-- INSERT INTO container(id, navigation_id, "type", resource_id, locale)
-- VALUES ('90683fdd-1650-4ef5-9f40-eda0bb16e134', 'd7318720-e807-409a-99c5-2ae2e7817289', 'ARTICLE', '54176cc9-c348-4240-ba5e-0d23affc6bd6', 'en_GB') ON CONFLICT DO NOTHING;
--
-- INSERT INTO article(id, name, contents, status)
-- VALUES ('54176cc9-c348-4240-ba5e-0d23affc6bd6', 'Test Page', '### Test Page
-- This is a test content page. There should be three additional test pages linked to this one.
--
-- #### Side Pages
-- Pages **Test Page 2** and **Test Page 3** should be linked in the sidebar.
-- #### Top pages
-- Page **Test Page 4** should be linked in the upper navigation bar.', 'ACTIVE') ON CONFLICT DO NOTHING;
--
-- INSERT INTO navigation (id, urn, full_urn, text, icon, ordinal, status, parent_id, type)
-- VALUES ('6726cc14-2c6c-48e2-8b04-f1ecbc229885', 'test', 'test/test', 'Test Page 2', 'test', 0, 'ACTIVE', 'd7318720-e807-409a-99c5-2ae2e7817289', 'SIDE') ON CONFLICT DO NOTHING;
--
-- INSERT INTO container(id, navigation_id, "type", resource_id, locale)
-- VALUES ('0cdc76df-886e-4122-8795-4507719cc875', '6726cc14-2c6c-48e2-8b04-f1ecbc229885', 'ARTICLE', '072b892d-5f3a-4537-bd76-7611e85f3d6b', 'en_GB') ON CONFLICT DO NOTHING;
--
-- INSERT INTO article(id, name, contents, status)
-- VALUES ('072b892d-5f3a-4537-bd76-7611e85f3d6b', 'Test Page 2', '### Test Page 2
-- This should be linked to **Test Page 1** via the side bar.', 'ACTIVE') ON CONFLICT DO NOTHING;
--
-- INSERT INTO navigation (id, urn, full_urn, text, icon, ordinal, status, parent_id, type)
-- VALUES ('a67748f7-538c-452c-b0dc-ac4ffdfa2130', 'other', 'test/other', 'Test Page 3', 'test', 1, 'ACTIVE', 'd7318720-e807-409a-99c5-2ae2e7817289', 'SIDE') ON CONFLICT DO NOTHING;
--
-- INSERT INTO container(id, navigation_id, "type", resource_id, locale)
-- VALUES ('3ba10052-7d91-4ace-bf28-d5a94f8c009c', 'a67748f7-538c-452c-b0dc-ac4ffdfa2130', 'ARTICLE', '7b5938e1-7db7-4d39-bde3-628c1f071cbe', 'en_GB') ON CONFLICT DO NOTHING;
--
-- INSERT INTO article(id, name, contents, status)
-- VALUES ('7b5938e1-7db7-4d39-bde3-628c1f071cbe', 'Test Page 3', '### Test Page 3
-- This should be linked to **Test Page 1** via the side bar.', 'ACTIVE') ON CONFLICT DO NOTHING;
--
-- INSERT INTO navigation (id, urn, full_urn, text, icon, ordinal, status, parent_id, type)
-- VALUES ('8206d105-6320-472f-b41b-c084bce076de', 'different', 'test/different', 'Test Page 4', 'test', 0, 'ACTIVE', 'd7318720-e807-409a-99c5-2ae2e7817289', 'TOP') ON CONFLICT DO NOTHING;
--
-- INSERT INTO container(id, navigation_id, "type", resource_id, locale)
-- VALUES ('31b4658f-23c2-4a41-a961-c531790e4074', '8206d105-6320-472f-b41b-c084bce076de', 'ARTICLE', '48483f08-1f0a-4f71-8b00-15fce3782de4', 'en_GB') ON CONFLICT DO NOTHING;
--
-- INSERT INTO article(id, name, contents, status)
-- VALUES ('48483f08-1f0a-4f71-8b00-15fce3782de4', 'Test Page 4', '### Test Page 4
-- This should be linked to **Test Page 1** via the navigation bar.', 'ACTIVE') ON CONFLICT DO NOTHING;

-- Home
--- About
--- Changelog
--- Roadmap
--- Code

-- System pages have no content, they only exist to support links for custom client pages
INSERT INTO navigation(id, urn, full_urn, system, text, icon, status)
VALUES ('0ed05608-701b-456c-ae12-50009e3fe9a9', '', '', true, 'Home', 'home', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation(id, parent_id, urn, full_urn, text, icon, status)
VALUES ('d7318720-e807-409a-99c5-2ae2e7817289', '0ed05608-701b-456c-ae12-50009e3fe9a9', 'about', 'about', 'About', 'info', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation_link(child_id, parent_id, ordinal, type, status)
VALUES ('d7318720-e807-409a-99c5-2ae2e7817289', '0ed05608-701b-456c-ae12-50009e3fe9a9', 0, 'SIDE', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO container(id, navigation_id, "type", resource_id, locale)
VALUES ('90683fdd-1650-4ef5-9f40-eda0bb16e134', 'd7318720-e807-409a-99c5-2ae2e7817289', 'ARTICLE', '54176cc9-c348-4240-ba5e-0d23affc6bd6', 'en_GB') ON CONFLICT DO NOTHING;

INSERT INTO article(id, name, contents, status)
VALUES ('54176cc9-c348-4240-ba5e-0d23affc6bd6', 'About', '##  The site
Nostalogic began as a technical experiment. Its purpose is to replicate various features found on other sites in an efficient, streamlined, modern web application. Current features include:

 - User registration and login
 - User groups
 - Access management for users and user groups
 - An article display system

This is a side project developed in free time so further development may be slow, but other planned features include:

 - A full content management system
 - A comment system for logged in users
 - A file upload system

## The technical details
At the core of Nostalogic is `nostalogic.io` - a REST API distributed across several microservices. The original core services are written in Kotlin and use the Spring framework. The frontend you''re looking at, `nostalogic.net`, is built in Angular.

## The creator
This site''s creator, known here as Eljee, is a software engineer with several years of experience working on web applications. ' ||
 'After working on existing projects, some built with old and out-dated technology, Eljee decided to create Nostalogic ' ||
 'as an attempt to replicate some of the most interesting or useful features seen in web applications with the ' ||
 'most modern technology available, built entirely from scratch.', 'ACTIVE') ON CONFLICT DO NOTHING;

-- About child links START
INSERT INTO navigation(id, parent_id, urn, full_urn, text, icon, status)
VALUES ('87927756-5aa5-440c-80dd-394e2992fe65', 'd7318720-e807-409a-99c5-2ae2e7817289', 'roadmap', 'about/roadmap', 'Roadmap', 'alt_route', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation_link(child_id, parent_id, ordinal, type, status)
VALUES ('87927756-5aa5-440c-80dd-394e2992fe65', 'd7318720-e807-409a-99c5-2ae2e7817289', 0, 'SIDE', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO container(id, navigation_id, "type", resource_id, locale)
VALUES ('f9951fa6-c426-45af-ab60-9feaa7c6aa94', '87927756-5aa5-440c-80dd-394e2992fe65', 'ARTICLE', 'a7a47cc3-6542-4970-ad81-5bb37a6c2f6a', 'en_GB') ON CONFLICT DO NOTHING;

INSERT INTO article(id, name, contents, status)
VALUES ('a7a47cc3-6542-4970-ad81-5bb37a6c2f6a', 'Roadmap', 'This roadmap describes some of the intended features for both the Nostalogic API (`nostalogic.io`) and this web client (`nostalogic.net`). While the web client is the primary user of the API, the API has been designed to be potentially very extendable and could be used to support additional projects beyond the main website, so these are considered separate projects here.

## API
##### CMS
The API has the basics of a content management service, or CMS, in place already - all the text on this page is being pulled from a database through the API. The ability to add or update page content is still missing though, so these must be added to complete the content service.
##### Documentation
A swagger based documentation system was previously in place before a complete rewrite of the API was done, but in this latest version this has been postponed until after the launch was completed.
##### File service
The ability to upload images and more generic files. This will be restricted to logged in users, along with some other limitations to prevent abuse.


## Web client
##### CMS
As with the API the client is also missing full CMS capabilities. Markdown has been chosen as the format for most content pages, and while plenty of parsers exist to allow the Angular client to convert that raw data to formatted **text** ~~like~~ `this`, there don''t seem to be many options for a prebuilt markdown editor for writing or editing content, so this will need to be built from scratch.', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation(id, parent_id, urn, full_urn, text, icon, status)
VALUES ('2e1add8c-e6e9-4583-9ac0-45176aae205a', 'd7318720-e807-409a-99c5-2ae2e7817289', 'changelog', 'about/changelog', 'Changelog', 'change_history', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation_link(child_id, parent_id, ordinal, type, status)
VALUES ('2e1add8c-e6e9-4583-9ac0-45176aae205a', 'd7318720-e807-409a-99c5-2ae2e7817289', 1, 'SIDE', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO container(id, navigation_id, "type", resource_id, locale)
VALUES ('0e4f2ffc-ae79-44dd-a020-c0c1ca3b0186', '2e1add8c-e6e9-4583-9ac0-45176aae205a', 'ARTICLE', '6b78270f-b3e6-4bca-9bca-cc262589cdc8', 'en_GB') ON CONFLICT DO NOTHING;

INSERT INTO article(id, name, contents, status)
VALUES ('6b78270f-b3e6-4bca-9bca-cc262589cdc8', 'Changelog', '## 0.1.0
### 2020-12-30
##### Key features
- Basic web client created
- Login & Registration feature implemented
- Users and group pages implemented
- Content retrieval from API implemented, including links for other pages in the side bar
- Three basic site themes are in place, these can be set for both logged in and logged out users from the profile page

##### Description
This is the go live of Nostalogic getting both the API and web client in a position to be properly released. This is expected to be flawed but usable, many features the backend is capable of aren''t fully implemented yet in order to bring the site live without further delay. Most significantly the backend can only do basic content retrieval based on some path, e.g. `about/changelog`, and the ability to edit or create this content isn''t in place in either the frontend or backend yet.', 'ACTIVE') ON CONFLICT DO NOTHING;
-- About child links END

INSERT INTO navigation(id, parent_id, urn, full_urn, text, icon, status)
VALUES ('2dc48ef3-6963-40b6-ab6b-574bd4a21d08', '0ed05608-701b-456c-ae12-50009e3fe9a9', 'code', 'code', 'Code', 'code', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation_link(child_id, parent_id, ordinal, type, status)
VALUES ('2dc48ef3-6963-40b6-ab6b-574bd4a21d08', '0ed05608-701b-456c-ae12-50009e3fe9a9', 1, 'SIDE', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO container(id, navigation_id, "type", resource_id, locale)
VALUES ('f865589c-540a-4614-9441-5bf487fc7856', '2dc48ef3-6963-40b6-ab6b-574bd4a21d08', 'ARTICLE', 'b6329d46-7b3f-4b96-aec8-0b9d74d32b5f', 'en_GB') ON CONFLICT DO NOTHING;

INSERT INTO article(id, name, contents, status)
VALUES ('b6329d46-7b3f-4b96-aec8-0b9d74d32b5f', 'Code', 'The code section is coming soon.

This will be some of the main content of this site after it goes live, there have been several hurdles to be overcome and interesting solutions to the problems involved in setting up a web applciation like this, so this will be the root section for several articles describing the solutions.

Firstly a separate Github account needs to be set up for this site to contain the data though, along with writing up the articles. ETA is 2020-02-01.', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation(id, parent_id, urn, full_urn, text, icon, status)
VALUES ('f356fc63-166a-4156-9cc6-e2c42194939e', '0ed05608-701b-456c-ae12-50009e3fe9a9', 'gallery', 'gallery', 'Gallery', 'collections', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation_link(child_id, parent_id, ordinal, type, status)
VALUES ('f356fc63-166a-4156-9cc6-e2c42194939e', '0ed05608-701b-456c-ae12-50009e3fe9a9', 2, 'SIDE', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO container(id, navigation_id, "type", resource_id, locale)
VALUES ('1b27513f-b604-4b76-9b3b-3248d2e1a8f0', 'f356fc63-166a-4156-9cc6-e2c42194939e', 'ARTICLE', 'b2fbb747-5272-4aec-b6e7-658ca185e35b', 'en_GB') ON CONFLICT DO NOTHING;

INSERT INTO article(id, name, contents, status)
VALUES ('b2fbb747-5272-4aec-b6e7-658ca185e35b', 'Gallery', 'A selection of images created during the development of Nostalogic.
### Banner Images
##### Origin
These banner images were created fairly late during the design process, hence why they include the name logo seen on the released version.

I had much more experience fiddling around in image editors to create things like this than in designing websites so my first instinct was to design a banner to be displayed at the top of the page as the focus and then let things flow from there. Over time I moved away from this idea since I felt it would detract from the site''s content, so the layout switched to intersecting navigation bars bordering the content taking up most of the page.

###### Blue Skies
![Blue Banner](https://nostalogic.s3-eu-west-1.amazonaws.com/images/preset/banners/Nosta_Banner_BlueSkies.png "Blue Banner")

An abstract blue themed banner, made with various "abstract" brush sets blended together.

###### Deus Expy
![Deus Expy](https://nostalogic.s3-eu-west-1.amazonaws.com/images/preset/banners/Nosta_Banner_DeusExpy.png "Deus Expy")

A dark orange design heavily inspired by the game *Deus Ex: Human Revolution* - look up any of that game''s art and you should see a dark murky background intersected with a few bright light sources and an overall dark orange colour scheme.

###### Nether Tiles
![Nether Tiles](https://nostalogic.s3-eu-west-1.amazonaws.com/images/preset/banners/Nosta_Banner_NetherTiles.png "Nether Tiles")

This is the main deviation in this set from being dominated by smooth lines. Underneath it''s still essentially a smooth abstract design similar to *Blue Skies*, but with a mosaic pattern applied on top. In fact I think the base image here is the same one used in *Blue Skies*, but with a second set of brushes on top and a green filter applied over it all. I couldn''t decide if I wanted the green to completely dominate the image or not so I made a variation:

###### Nether Tiles 2
![Nether Tiles 2](https://nostalogic.s3-eu-west-1.amazonaws.com/images/preset/banners/Nosta_Banner_NetherTiles2.png "Nether Tiles 2")

Here the green filter isn''t applied so you can more clearly see that there''s a separate green abstract design on top of a blue one, blended together so the green only shows in the dark areas.

###### Green Arcane
![Green Arcane](https://nostalogic.s3-eu-west-1.amazonaws.com/images/preset/banners/Nosta_Banner_GreenArcane.png "Green Arcane")

Since all the other designs were purely abstract this was an attempt to add some variation with solid, distinct objects. Originally actual text was used, I think I tried both code samples and meaningless "lorem ipsum" text, but both drew too much attention from what''s meant to be a pretty looking image marked with the site name. In the end some brushes with meaningless "arcane" symbols were used in multiple layers at different sizes.

###### Red Electric
![Red Electric](https://nostalogic.s3-eu-west-1.amazonaws.com/images/preset/banners/Nosta_Banner_RedElectric.png "Red Electric")

This began with just the lightning effect from some brushes over a plain background. When it came to adding further details I tried a few things but ultimately thought the lightning effect could almost carry the entire image, so a faint cloud layer was added behind it to support some colour shade variation and it was left at that.

###### Big Blue
![Big Blue](https://nostalogic.s3-eu-west-1.amazonaws.com/images/preset/banners/banner2.png "Big Blue")

The other banners were deliberately created the same size with the same name logo in the bottom left, with the intent that they could be a set with one randomly displayed each time the page was loaded. This completely plain abstract image was from about a year earlier before that idea came about.


### The Early Version
##### Origin
Long before deciding to build a whole web application from scratch, `nostalogic.net` existed as a host for a PHPBB forum I was using as a personal website, with a basic splash page linking to that forum (at a subdirectory). When I first thought about expanding the splash page to something with more function I initially tried using PHP since it was a language I had never used but knew it was very common in web design. This screenshot of a work in progress PHP login system is as far as that version went:

![PHP Nostalogic](https://nostalogic.s3-eu-west-1.amazonaws.com/images/preset/designs/capture1.png "PHP Nostalogic")

I had already begun using Angular for other projects around this time (the early version, what''s now known as AngularJS) and found it a lot more appealing than this PHP experiment. Both the language and this top to bottom interface design were abandoned here.', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation(id, parent_id, urn, full_urn, text, icon, status)
VALUES ('cae76a5f-7951-4163-8708-b3f43ff43d57', '0ed05608-701b-456c-ae12-50009e3fe9a9', 'chat', 'chat', 'Chat', 'chat', 'INACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO navigation_link(child_id, parent_id, ordinal, type, status)
VALUES ('cae76a5f-7951-4163-8708-b3f43ff43d57', '0ed05608-701b-456c-ae12-50009e3fe9a9', 2, 'SIDE', 'ACTIVE') ON CONFLICT DO NOTHING;

INSERT INTO container(id, navigation_id, "type", resource_id, locale)
VALUES ('5d8653d9-7cdf-4605-9396-0c8a045362b6', 'cae76a5f-7951-4163-8708-b3f43ff43d57', 'ARTICLE', 'b1eaebc6-69b3-4f8d-8e19-ffcffe4b0746', 'en_GB') ON CONFLICT DO NOTHING;

INSERT INTO article(id, name, contents, status)
VALUES ('b1eaebc6-69b3-4f8d-8e19-ffcffe4b0746', 'Chat', 'There is a Nostalogic discord server which is freely available for all users:

https://discord.gg/cMrdBTYfr8', 'ACTIVE') ON CONFLICT DO NOTHING;


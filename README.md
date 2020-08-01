# Code Insights Extension for BitBucket Server

## Introduction
With the release of BitBucket Server 7, repository administrators now have the ability to configure compulsory
insight reports that must report their status before merging can take place. An administrator also has the ability
to list which of these reports must pass.

This has been a great addition and, in some ways, brings the code insights feature up to par with similar GitHub and 
GitLab features.

However, there is one thing that is not yet implemented - the ability for an administrator to 'force' the merge
from the UI by using their admin privileges. This feature is important because sometimes a failed status check is
a risk that a product owner or chief engineer is willing to accept in order to resolve a different issue. 

Example of functionality in a GitHub pull request:

![KNH6q](https://user-images.githubusercontent.com/38512252/86992345-a4cfaf80-c1e4-11ea-888e-cc3a190641dc.png)

Examples that spring to mind are issues in common libraries, that are medium severity and have no ETA on a fix. 
If a second issue is found in a critical area of the codebase (say an exploit that allowed anyone to login as anyone else),
the decision would probably be made to move ahead with the merge to fix the critical bug immediately instead of waiting for
the medium severity issue to be fixed.

This extension adds the ability for a repository administrator to override a blocked merge in these situations.

## Images
<img width="502" alt="Screen Shot 2020-07-09 at 1 05 15 pm" src="https://user-images.githubusercontent.com/38512252/86992471-e4969700-c1e4-11ea-86b8-4a05800cfd1b.png">

<img width="829" alt="Screen Shot 2020-07-09 at 1 05 24 pm" src="https://user-images.githubusercontent.com/38512252/86992516-fe37de80-c1e4-11ea-816f-f5143db1e57d.png">

## Usage
This extension is not available on the BitBucket Server market place (yet). An administrator could install the 
extension to their BitBucket Server by first cloning this repository and following the below steps:

1. Install and configure the Atlassian SDK. Instructions [here](https://developer.atlassian.com/server/framework/atlassian-sdk/install-the-atlassian-sdk-on-a-linux-or-mac-system/)
2. Clone this repository to your machine.
3. Run the command `atlas-package` from within the project directory. A jar of this plugin will be created at src/target/.
4. Install the plugin in your BitBucket Server using the [Universal Plugin Manager](https://confluence.atlassian.com/bitbucketserver/managing-apps-776640366.html?_ga=2.81479828.2001798493.1594262869-1170595930.1590906581)

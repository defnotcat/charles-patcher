# Charles Proxy Patcher

Removes the Charles Proxy 30 minutes session limit.

Should support any version of https://charlesproxy.com above 4.0

**Requirements**

 - Java 8 or above
 
**Usage**


    D:\>java -jar charles-patcher.jar
    Charles Proxy Patcher - v1.0
    Once applied, Charles will no longer have the 30 minutes session limitation.
    You'll have to replace charles.jar (Can be found in C:/Program Files/Charles/lib or wherever you installed it) by the patched one manually.
    
    Non-Patched File: charles.jar
    Output File: charles patched.jar
    [INFO] Charles Version: 4.2.8
    [INFO] Patch applied, moving to output directory..
    [INFO] Job done, file patched in 1 second(s).
    [INFO] You can find the patched file at charles_patched.jar
    
Once you've got the patched file, copy it to `C:\Program Files\Charles\lib\charles.jar` (Or in `Your_Charles_Root/lib/charles.jar`) and start Charles like you usually do.
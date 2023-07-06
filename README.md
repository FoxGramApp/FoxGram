# 🎨 **Colorgram**
### Colorgram is an unofficial application that uses _Telegram's API_.

![Colorgram](img/color_banner.png)

## 🌐 Reproducible Builds
To reproduce the build of Colorgram is only needed ccache (**already installed in "Tools" folder**), on macOs
will be used from Homebrew if installed, otherwise it will be used the one in the "Tools" folder.

1. [**Obtain your own api_id**](https://core.telegram.org/api/obtaining_api_id) for your application and put [**here**](https://github.com/Pierlu096/Color/blob/dev/TMessagesProj/src/main/java/org/telegram/messenger/BuildVars.java). 
2. Create 2 app in [**firebase**](https://console.firebase.google.com/) and download google_services.json file.
3. Add your google-services.json file to the [**root of the project**](https://github.com/Pierlu096/Color/blob/dev/TMessagesProj_App/google-services.json).
4. Copy your release.keystore [**here**](https://github.com/Pierlu096/Color/blob/dev/TMessagesProj/config/release.keystore).
5. Add `MAPS_API_KEY=<your-api-key>` (you can get it [**here**](https://console.cloud.google.com/google/maps-apis)) to your `local.properties` file.
6. Please **do not** use the name Telegram for your app — or make sure your users understand that it is unofficial.
7. Kindly **do not** use our standard logo (white paper plane in a blue circle) as your app's logo.
8. Please study our [**security guidelines**](https://core.telegram.org/mtproto/security_guidelines) and take good care of your users' data and privacy.
9. Please remember to publish **your** code too in order to comply with the licences.

## ✨**Colorgram remote assets**
All remote assets that Colorgram uses are [**here**](https://github.com/Pierlu096/colorgramserver), if you want to visit or made a fork.

## ✅ Thanks to
• [**Nekogram**](<https://gitlab.com/Nekogram/Nekogram>)  
• [**CatoGramX**](<https://github.com/CatogramX/CatogramX>)

## 🦉 **Thanks to [**OwlGram**](<http://github.com/OwlgramDev/OwlGram>)**
[*Licensed under GNU GPL-2.0*](<https://github.com/Pierlu096/Colorgram/blob/dev/LICENSE>)
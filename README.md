# AllenRetrofit
Improved Type-safe HTTP client based on Retrofit for Android and Java by HiWong.

Undoubtedly,Retrofit is an excellent type-safe http client. Yet there are still some details need to be improved. The first shortingcoming 
is that Retrofit parsing http info by reflection instead of annotation, which has been proved to be more effective cause it 
parsing http info in build-time other than run-time. 

Another is that developers have to write a Client class for every http interface. That's really frustrating cause most of which are 
repeated work.

Based on that 2 blind sides, I created my own http client i.e AllenRetrofit.

As you can see, unit tests and demos have not be accomplished. And that's why I haven't submitted it to jcenter or maven.

I will add them soon. 

#License
=========

Copyright 2016 HiWong(bettarwang@gmail.com).

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

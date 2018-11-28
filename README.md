# ExoPlayerVideoBlur
## Gussin

该Demo文章地址：https://blog.csdn.net/liosen/article/details/83896721

### 效果图
<div>
<img src="https://github.com/liosen/ExoPlayerVideoBlur/blob/master/img/befor.png" width="30%" alt="图片加载失败时"/>

<img src="https://github.com/liosen/ExoPlayerVideoBlur/blob/master/img/after.png" width="30%" alt="图片加载失败时"/>
</div>

|名词|解释|
|:---|:---|
|radius|偏移量|
|blurX|X轴方向偏移次数|
|blurY|Y轴方向偏移次数|
|trans|亮度|

 原理大概是：

将原图层亮度调为原来的 trans倍(trans为自定义参数，默认0.005)，然后copy一层X轴方向偏移，copy一层Y轴偏移，偏移量为radius。

画个图解释一下

<img src="https://github.com/liosen/ExoPlayerVideoBlur/blob/master/img/图解.png" alt="图片加载失败时"/>

贴出关键滤镜代码
```
#GaussianBlurEffect
#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;
const float resolution=1024.0;
const float radius = radius;
vec2 dir = vec2(1.0,1.0);
    void main() {
    vec4 sum = vec4(0.0);
    vec2 tc = vTextureCoord;
    float blur = radius/resolution;
    float hstep = dir.x;
    float vstep = dir.y;
    int x = blurX;
    int y = blurY;
    for(int i = x;i > 0;i--){ 
    	for(int j = y; j > 0; j--){
    	    sum = texture2D(sTexture, vec2(tc.x + float(i)*blur*hstep, tc.y + float(j)*blur*vstep)) *trans;
	    sum = texture2D(sTexture, vec2(tc.x - float(i)*blur*hstep, tc.y + float(j)*blur*vstep)) *trans;
	    sum = texture2D(sTexture, vec2(tc.x - float(i)*blur*hstep, tc.y - float(j)*blur*vstep)) *trans;
	    sum = texture2D(sTexture, vec2(tc.x + float(i)*blur*hstep, tc.y - float(j)*blur*vstep)) *trans;
    	}
    }
    vec4 cc= texture2D(sTexture,vTextureCoord );
 
    gl_FragColor =vec4(sum.rgb, cc.a);
    }
```

其他具体内容，请移步博客：
https://blog.csdn.net/liosen/article/details/83896721




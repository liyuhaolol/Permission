package spa.lyh.cn.permission.bean;

import java.util.List;

public class PictureBean {
   private String name;//文件夹的名字，以后会用到，具体用处可以先打开微信的图片选择器，点左下角视频和图片，弹出一个popupwindow，每一个item都有一个name，他这个name应该是图片父目录的文件名，我这个name也是这个意思，不过这篇博客应该暂时用不到
   private int number;//这个文件夹下图片的数量
   private String firstImageUrl;//这个文件夹下的第一张图片
   private List<String>  imageList;//这个文件夹下的图片集合
   private String parentPath;//这个文件夹的绝对路径

   public String getParentPath() {
      return parentPath;
   }

   public void setParentPath(String parentPath) {
      this.parentPath = parentPath;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public int getNumber() {
      return number;
   }

   public void setNumber(int number) {
      this.number = number;
   }

   public String getFirstImageUrl() {
      return firstImageUrl;
   }

   public void setFirstImageUrl(String firstImageUrl) {
      this.firstImageUrl = firstImageUrl;
   }

   public List<String> getImageList() {
      return imageList;
   }

   public void setImageList(List<String>  imageList) {
      this.imageList = imageList;
   }
}

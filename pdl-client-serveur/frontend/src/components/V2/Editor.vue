<script setup lang="ts">
  import { defineProps } from 'vue';
  import { api } from '@/http-api';
  import router from "@/router";
  import { ref } from 'vue';
  import { ImageType } from '@/image'
  import { defineComponent } from 'vue'
  import { createToast } from 'mosha-vue-toastify';
  import 'mosha-vue-toastify/dist/style.css';

  //https://vuejsexamples.com/a-lightweight-toast-or-notification-or-snack-bar-for-vue3/

  const img1 = ref(-1);
  const img2 = ref(-1);

  const img1_bis = ref("");
  const img2_bis = ref("");

  const lineX = ref(0);
  const lineY = ref(0);

  const lineS = ref(100);

  var cpt1 = true;
  var cpt2 = true;

  const imageList = ref<ImageType[]>([]);
  getImageList();

  function getImageList() {
    api.getImageList().then((data) => {
      imageList.value = data;
    }).catch(e => {
      console.log(e.message);
      createToast({ title: ''+ e.message , description: 'Impossible de charger les images'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

    });
  }

    
  function superposeImages() {

    const start = document.getElementById('btn_start');

    if (start != null){

      start.style.display = 'inline';
      createToast({ title: 'info' , description: 'Superposition OK'} , {toastBackgroundColor : 'rgb(0, 128, 0)', type : 'info', timeout : 2000, position : 'top-center', showIcon : true});

    }else{

      console.log('error : function superposeImages()');
      createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

    }

  }

  function Console(){

    const back = <HTMLImageElement> document.getElementById('back');
    const front = <HTMLImageElement> document.getElementById('front');

    const sizeX = <HTMLInputElement> document.getElementById('sliderX');
    const sizeY = <HTMLInputElement> document.getElementById('sliderY');

    const sizeS = <HTMLInputElement> document.getElementById('sliderS');

    const right = document.getElementById('right');
    const cons = document.getElementById('console');
    const size = document.getElementById('sliderS');

    const save = document.getElementById('btn_save');

    const start = document.getElementById('btn_start');

    if (back != null && front != null && right != null && cons != null && save != null && size != null && start != null){

      cons.style.display = 'flex'; // attention
      size.style.display = 'inline';
      right.style.display = 'inline';
      save.style.display = 'inline';

      start.style.display = 'none';

      var cpt1 = back.naturalWidth-front.naturalWidth; 
      var cpt2 = back.naturalHeight-front.naturalHeight; 

      sizeX.max = cpt1.toString();
      sizeY.max = cpt2.toString();

      if (back.naturalWidth < back.naturalHeight){
        sizeS.max = back.naturalWidth.toString();
      }else{
        sizeS.max = back.naturalHeight.toString();
      }

      modif();

    }else{

      console.log('error : function Console()');
      createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

    }
  }

  // on s'occupe de l'emplacement de l'image
  function modif(){

    var back = <HTMLImageElement> document.getElementById("back");
    var front = <HTMLImageElement> document.getElementById("front");

    var sizeX = <HTMLInputElement> document.getElementById('sliderX');
    var sizeY = <HTMLInputElement> document.getElementById('sliderY');

    if (back != null && front != null && sizeX != null && sizeY != null){

      front.style.top = lineY.value.toString()+'px';
      front.style.left = lineX.value.toString()+'px';

    }else{
      console.log('error : function modif()');
      createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

    }
  }

  var save_lineS = 0;

  //on s'occupe de la taille de l'image
  function set_size(){

    var back = <HTMLImageElement> document.getElementById("back");
    var front = <HTMLImageElement> document.getElementById("front");

    var sizeX = <HTMLInputElement> document.getElementById('sliderX');
    var sizeY = <HTMLInputElement> document.getElementById('sliderY');

    if (back != null && front != null && sizeX != null && sizeY != null){

      var save_size_width = front.width;
      var save_size_height = front.height;

      if (back.naturalWidth < back.naturalHeight){

        front.style.width = lineS.value.toString()+'px';
        front.style.height = 'auto';

        console.log("here");

        }else{

        front.style.height = lineS.value.toString()+'px';
        front.style.width = 'auto';

        console.log("here2");

        }

        console.log("back = " + back.width + ", "+ back.height + " / front = " + front.width +", " + front.height +
        " / position = " + positionX() + ", " + positionY() + " / check = " +  (positionX() + front.width) + ", " + (positionY() + front.height) );

      //on fait un check pour verifier que l'image ne sort pas du canvas
      if (((positionY() + front.height) > back.height) || ((positionX() + front.width) > back.width)){

        console.log('on sort du canvas');
        createToast({ title: 'error', description: 'Votre image sort des limites'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

        front.style.width = save_size_width + "px";
        front.style.height = save_size_height + "px";
        lineS.value = save_lineS;
        
      }

    }else{

      console.log('error : function set_size()');
      createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

    }

    var cpt1 = back.naturalWidth - front.width; 
    var cpt2 = back.naturalHeight - front.height; 

    sizeX.max = cpt1.toString();
    sizeY.max = cpt2.toString();

    save_lineS = lineS.value;

  }

  function getImageName(id: number): string {
    const image = imageList.value.find(img => img.id === id);
    if (image) {
      return image.name;
    } else {
      return "undefined";
    }
  }

  function set_image(cpt : boolean){ // on remet tout a 0

    const right = document.getElementById('right');
    const cons = document.getElementById('console');

    const start = document.getElementById('btn_start');
    const save = document.getElementById('btn_save');

    const sizeS = <HTMLInputElement> document.getElementById('sliderS');

    if(right != null && cons != null && start != null && save != null && sizeS != null){

      right.style.display = 'none';
      cons.style.display = 'none';

      start.style.display = 'none';
      save.style.display = 'none';

      sizeS.style.display = 'none';

    }else{

      console.log('error : function set_image()');
      createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

    }

    //On change le backend
    if (cpt == true){ 

      img1_bis.value = 'images/'+ img1.value.toString();

      createToast({ title: 'info' , description: 'Background ok'} , {toastBackgroundColor : 'rgb(0, 128, 0)', type : 'info', timeout : 2000, position : 'top-center', showIcon : true});

      cpt1 = false;

    }

    //On change le frontend
    else if (cpt == false){

      img2_bis.value = 'images/'+ img2.value.toString();

      createToast({ title: 'info' , description: 'Foreground ok'} , {toastBackgroundColor : 'rgb(0, 128, 0)', type : 'info', timeout : 2000, position : 'top-center', showIcon : true});
      
      cpt2 = false;
    }

    // Les deux images sont présentent
    if (cpt1 == false && cpt2 == false){

      createToast({ title: 'info' , description: 'Please wait...'} , {toastBackgroundColor : 'rgb(0, 128, 0)', type : 'info', timeout : 2000, position : 'top-center', showIcon : true});

      superposeImages();

    }

    lineX.value = 0;
    lineY.value = 0;
    lineS.value = 100;

  }

  function positionX():number{

    const front = <HTMLImageElement> document.getElementById('front');

    if (front != null){
      var cpt = parseInt(lineX.value.toString());
      return cpt;

    }else{

      console.log('error : function positionX()');

      createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

    }

    return -1;
  }

  function positionY():number{

    const front = <HTMLImageElement> document.getElementById('front');

    if (front != null){
      
      var cpt = parseInt(lineY.value.toString());

      return cpt;

    }else{

      console.log('error : function positionY()');

      createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

    }

    return -1;
  }

  async function save(){

    var front = <HTMLImageElement> document.getElementById("front");

    console.log('image back : ' + img1.value + ', image front : '+ img2.value 
    +', pos : ' + positionX() + ', ' + positionY() 
    + ', new size : '+ front.width + ', ' + front.height);

    var last_tab : number[] = [];

    last_tab.push(positionX());
    last_tab.push(positionY());
    last_tab.push(parseInt(front.width.toString()));
    last_tab.push(parseInt(front.height.toString()));

    await api.ChangeImage(img1.value, "Edit" , img2.value , -1, last_tab); 

    //location.reload();
    createToast({ title: 'info', description: "L'image a été modifié, vous pouvez la visualiser dans la galerie"} , {toastBackgroundColor : 'rgb(0,128,0)', type : 'info', timeout : 5000, position : 'top-center', showIcon : true});

  }

</script>

<template>
<div>
  <h3>Editor</h3>

  <div id = "panel">

    <div id = "left">

      <p>Merci de selectionner deux images à superposer.</p>

      Background :
      <select v-model="img1" @change="set_image(true)">
        <option v-for="image in imageList" :value="image.id" :key="image.id">{{ image.name }}</option>
      </select>

      <br>
      <br>
      
      Foreground : 
      <select v-model="img2" @change="set_image(false)">
        <option  v-for="image in imageList" :value="image.id" :key="image.id">{{ image.name }}</option>
      </select>

      <br>
      <br>

      <button id = "btn_start" @click="Console(), set_size()">Start</button>

      <div id="console">

        <input id = "sliderX" type="range" min="1" max="100" v-model="lineX" @mousemove="modif()" >

        <input id = "sliderY" type="range" min="1" max="100" v-model="lineY" @mousemove="modif()">

      </div>

      <br>

      <input id = "sliderS" type="number" min="1" v-model="lineS" @change="set_size()">

      <button id = "btn_save" @click="save">Save</button>

    </div>

    <div id = "right">

      <img id = "back" :src="img1_bis">
      <img id = "front" :src="img2_bis">

    </div>

  </div>

</div> 
</template>

<style scoped>

  #panel{
    display : flex;
    justify-content:center;
    align-items: center;
    flex-wrap:wrap;
    

    background-color: rgb(218, 218, 218);
    padding:10px;
    border-radius: 10px;
  }

  #left{
    margin: 20px;
  }

  #right{
    margin: 20px;

    position: relative;

    display: none;

  }

  #sliderX{
    width:80px;
  }

  #sliderY{
    transform:rotate(90deg); 
    height: 100px; 
    width: 80px
  }


  #front {
    position: absolute;
    top:0;
    left:0;
    width: 0px;
    height : 0px;
  }

  #back{
    border: 1px solid black;
    z-index: 1;

  }

  #btn_start{
    display:none;
  }

  #btn_save{
    display:none;
  }

  #console{
    display : none;
    justify-content:center;
    align-items: center;
    flex-wrap:wrap;

    background-color: rgb(255, 255, 255);
    border-radius: 10px;

    margin-top:20px

  }

  #sliderS{
    display: none;
  }

</style>


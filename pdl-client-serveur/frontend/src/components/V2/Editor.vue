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

    const back = <HTMLImageElement> document.getElementById('back');
    const front = <HTMLImageElement> document.getElementById('front');

    const start = document.getElementById('btn_start');

    if (back != null && front != null && start != null){

      console.log('The back image size is '+ back.naturalWidth +' x '+ back.naturalHeight);
      console.log('The front image size is '+ front.naturalWidth +' x '+ front.naturalHeight);

      if (back.naturalWidth > front.naturalWidth && back.naturalHeight > front.naturalHeight){
        console.log("Les images ont les bonnes dimension");

        start.style.display = 'inline';

      }else{
        createToast({ title: 'error', description: "L'image du backend soit avoir une taille supérieur à celle du frontend"} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});
      
        const right = document.getElementById('right');
        const cons = document.getElementById('console');

        if(right != null && cons != null){
          right.style.display = 'none';
          cons.style.display = 'none';

          cpt1 = true;
          cpt2 = true;

        }else{

          console.log('error : function calcul_size()');
          createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

        }
      }
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

    const right = document.getElementById('right');
    const cons = document.getElementById('console');

    const save = document.getElementById('btn_save');

    if (back != null && front != null && right != null && cons != null && save != null){

      cons.style.display = 'flex'; // attention
      right.style.display = 'inline';
      save.style.display = 'inline';

      var cpt1 = back.naturalWidth-front.naturalWidth; 
      var cpt2 = back.naturalHeight-front.naturalHeight; 

      sizeX.max = cpt1.toString();
      sizeY.max = cpt2.toString();

    }else{

      console.log('error : function Console()');
      createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

    }

  }

  function modif(){
    var size = document.getElementById("front");
    
    if (size != null){

      size.style.top = lineY.value.toString()+'px';
      size.style.left = lineX.value.toString()+'px';

    }else{
      console.log('error : function modif()');
      createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

    }
  }

  function calcul_size(cpt : boolean){

    const right = document.getElementById('right');
    const cons = document.getElementById('console');

    const start = document.getElementById('btn_start');
    const save = document.getElementById('btn_save');

    if(right != null && cons != null && start != null && save != null){
      right.style.display = 'none';
      cons.style.display = 'none';

      start.style.display = 'none';
      save.style.display = 'none';

    }else{

      console.log('error : function calcul_size()');

      createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

    }

    //On change le backend
    if (cpt == true){ 

      img1_bis.value = 'images/'+ img1.value.toString();

      createToast({ title: 'info' , description: 'Backend ok'} , {toastBackgroundColor : 'rgb(0, 128, 0)', type : 'info', timeout : 2000, position : 'top-center', showIcon : true});

      cpt1 = false;

    }
    //On change le frontend
    else if (cpt == false){

      img2_bis.value = 'images/'+ img2.value.toString();

      createToast({ title: 'info' , description: 'Frontend ok'} , {toastBackgroundColor : 'rgb(0, 128, 0)', type : 'info', timeout : 2000, position : 'top-center', showIcon : true});

      cpt2 = false;
    }

    // Les deux images sont présentent
    if (cpt1 == false && cpt2 == false){

      createToast({ title: 'info' , description: 'Please wait'} , {toastBackgroundColor : 'rgb(0, 128, 0)', type : 'info', timeout : 2000, position : 'top-center', showIcon : true});

      setTimeout(function() {

        superposeImages();

      }, 2000);
    }

  }

  function positionX():number{

    const front = <HTMLImageElement> document.getElementById('front');

    if (front != null){
      var cpt = parseFloat(lineX.value.toString()) + front.naturalWidth/2;
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
      
      var cpt = parseFloat(lineY.value.toString()) + front.naturalHeight/2;

      return cpt;

    }else{

      console.log('error : function positionY()');

      createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

    }

    return -1;
  }

  async function save(){

    await api.ChangeImages(img1.value, img2.value, "Edit" , positionX() , positionY()); 

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

      Backend 
      <select v-model="img1" @change="calcul_size(true)">
        <option v-for="image in imageList" :value="image.id" :key="image.id">{{ image.name }}</option>
      </select>

      <br>
      <br>

      Frontend : 
      <select v-model="img2" @change="calcul_size(false)">
        <option v-for="image in imageList" :value="image.id" :key="image.id">{{ image.name }}</option>
      </select>

      <br>
      <br>

      <button id = "btn_start" @click="Console">Start</button>

      <div id="console">

        <input id = "sliderX" type="range" min="1" max="100" v-model="lineX" @mousemove="modif()">

        <input id = "sliderY" type="range" min="1" max="100" v-model="lineY" @mousemove="modif()">


      </div>

      <br>
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

</style>


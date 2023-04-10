<script setup lang="ts">
  import VueDrawingCanvas from "vue-drawing-canvas";
  import { ref, onMounted } from 'vue';
  import { api } from '@/http-api';
  import { createToast } from 'mosha-vue-toastify';
  import 'mosha-vue-toastify/dist/style.css';

  const textId = ref("");
  const colorId = ref("#000000");
  const lineId = ref(5);
  const eraserId = ref(false); // gommme
  const lineTypeId = ref("round");
  const initialImageId = ref([]);
  const disabledId = ref(false);
  const strokeTypeId = ref("dash");
  const fillShapeId = ref(false);
  const backgroundColorId = ref("#FFFFFF");

  const x = ref(0);
  const y = ref(0);

  const widthId = ref(100);
  const heightId = ref(100);  

  //image setting
  const typeId = ref("png");
  const imageId = ref("");

  const VueCanvasDrawing = ref()

  const Quality = ref(0.75);

  //---------------------------------------------------//

  function Undo(){
    VueCanvasDrawing.value.undo();
  }

  function Redo(){
    VueCanvasDrawing.value.redo();
  }

  function New_page(){
    VueCanvasDrawing.value.reset();
  }

  onMounted(() => { // super utile
    Refresh(false)
    displayWindowSize()

  })

  async function Refresh(value : boolean){
    const upt = document.getElementById("click-to-update");
    
    if (upt != null){
      if (value == false){
        upt.style.display = "none";
      }else{
        upt.style.display = "inline";
      }
    }else{
      console.log("une erreur est survenue");
      createToast({ title: 'error' , description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

    }
  }

  //---------------------------------------------------//

  async function Save() {

    if (textId.value == ""){
      createToast({ title: 'info' , description: 'Vous devez mettre un titre'} , {toastBackgroundColor : 'rgb(0, 128, 0)', type : 'info', timeout : 5000, position : 'top-center', showIcon : true});

    }else{
      const base64 = await fetch(imageId.value);

      const blob = await base64.blob();

      const formData = new FormData();
      formData.append("file", blob, textId.value + '.' +typeId.value); 

      api.createImage(formData)
        .then(() => {
          console.log("success");
        })
        .catch((e) => {
          console.log(e.message);
          createToast({ title: ''+ e.message , description: 'Impossible de charger les images'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

        });
    }
  }


  function getCoordinate(event: any) {
      let coordinates = VueCanvasDrawing.value.getCoordinates(event);
      x.value = coordinates.x;
      y.value = coordinates.y;
  }

  //---------------------------------------------------//

  async function convertBlobPngToJpg(blob : Blob) {
    const img = new Image();
    img.src = URL.createObjectURL(blob);

    await new Promise((resolve) => {
      img.onload = resolve;
    });

    const canvas = document.createElement('canvas');
    canvas.width = img.width;
    canvas.height = img.height;

    const ctx = canvas.getContext('2d');
    ctx?.drawImage(img, 0, 0);

    const jpegBlob = await new Promise((resolve) => {
      canvas.toBlob((blob) => {
        resolve(blob);
      }, 'image/jpeg', 0.75);
    });
    
    return jpegBlob;
  }

  async function downloadFile() {

    if (textId.value == ""){
      createToast({ title: 'info' , description: 'Vous devez mettre un titre'} , {toastBackgroundColor : 'rgb(0, 128, 0)', type : 'info', timeout : 5000, position : 'top-center', showIcon : true});
    }else{      
      
    const response = await fetch(imageId.value);

    var blob = null;

    if (typeId.value == 'jpg'){
      blob = await convertBlobPngToJpg( await response.blob());

    }else{
      blob = await response.blob();
    }
    
    const link = document.createElement("a");
    if (blob instanceof Blob) {
      link.href = URL.createObjectURL(blob);
    }
    link.download = textId.value;
    link.click();

    URL.revokeObjectURL(link.href);
    
    }
  }

  function displayWindowSize(){
    var ipt1 = <HTMLInputElement> document.getElementById("input_x");
    var ipt2 = <HTMLInputElement> document.getElementById("input_y");

    if (ipt1 != null && ipt2 != null){
      ipt1.max = (window.innerWidth - 50).toString();
      ipt2.max = (window.innerWidth - 50).toString();

      widthId.value = 100;
      New_page();

    }else{
      console.log("error : function displayWindowSize");
      createToast({ title: 'error' , description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

    }

  }

  window.addEventListener("resize", displayWindowSize);

  //---------------------------------------------------//

  function fun_quatity(){
    var ipt = document.getElementById("format");

    if (ipt != null){
      if (typeId.value == 'png'){
        ipt.style.display = 'none';
      }else{
        ipt.style.display = 'inline'
        createToast({ title: 'info' , description: "Vous pouvez modifier la qualité de l'image"} , {toastBackgroundColor : 'rgb(0, 128, 0)', type : 'info', timeout : 5000, position : 'top-center', showIcon : true});

      }
    }else{
      console.log("error : function fun_quatity");
      createToast({ title: 'error' , description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

    }
  }

</script>

<template>

<div>
  <h3>Paint</h3>

  <input id = "input_x" type="range" min="10" @change="New_page()" v-model="widthId">

  <input id = "input_y" type="range" min="10" @change="New_page()" v-model="heightId">

  <br>
  {{ widthId }} x {{ heightId }}

    <br>
    <br>

    <vue-drawing-canvas ref="VueCanvasDrawing"
      
      v-model:image="imageId"

      :width="widthId"
      :height="heightId"
      :eraser="eraserId"
      :color="colorId"
      :lineWidth="lineId"
      :line-join="lineTypeId"
      :saveAs="typeId" 
      :initial-image="initialImageId"
      :stroke-type="strokeTypeId"
      :fill-shape="fillShapeId"

      :background-color="backgroundColorId"

      :lock="disabledId"

      :styles="{
        border: 'solid 6px #000',
      }"

      @mousemove="getCoordinate($event)"
      @click.prevent="Refresh(false)"

    />

    <br>

    <p>
      x-axis: <strong>{{ x }}</strong>, y-axis: <strong>{{ y }}</strong>
    </p>

    <div id="click-to-update" >
      <p>
        &#128070 Click on the canvas to update &#128070
      </p>
    </div>

    <!--     
    <img :src="imageId" style="border: solid 6px #000000"> 
    <br> -->

    <input type="color" v-model="colorId" />
    <input type="color" v-model="backgroundColorId" @change="Refresh(true)"/>

    <select v-model="eraserId">
        <option :value="false"> 
          crayon
        </option>  
        <option :value="true"> 
          gomme
        </option>  
    </select>  

    <br>
    <hr>

    <input type="range" min="1" max="100" v-model="lineId">

    <br>
    <hr>

    <button @click.prevent="Undo()">
      Undo
    </button>
    <button @click.prevent="Redo()">
      Redo
    </button>
    <button @click.prevent="New_page()">
      New
    </button>

    <br>
    <hr>
    <input type="text" style="width:100px" v-model="textId">

    <select v-model="typeId" name="format" @change="fun_quatity()">
      <option :value="'png'">png</option>
      <option :value="'jpg'">jpg</option>
    </select>

    <button type="button" @click.prevent="Save()">
      submit
    </button>

    <button type="button" @click.prevent="downloadFile()">
      download file
    </button>

    <div id="format">
      <hr>
      <input type="range" min="0" max="1"  step="0.05" v-model="Quality">
    </div>

</div> 
</template>

<style scoped>

  input{
    margin: 10px;
  }
  select{
    margin:10px;
  }
  button{
    margin:10px;
  }

  hr{
    width: 300px;

  }

  #format{
    display: none;
  }

</style>


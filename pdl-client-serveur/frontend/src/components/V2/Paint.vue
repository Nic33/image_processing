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

  const widthId = ref(600);
  const heightId = ref(300);  

  //image setting
  const typeId = ref("png");
  const imageId = ref("");

  const VueCanvasDrawing = ref()

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
      formData.append("file", blob, textId.value + ".png"); 

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

  function downloadFile() {

    if (textId.value == ""){
      createToast({ title: 'info' , description: 'Vous devez mettre un titre'} , {toastBackgroundColor : 'rgb(0, 128, 0)', type : 'info', timeout : 5000, position : 'top-center', showIcon : true});
    }else{
      var a = document.createElement("a"); //Create <a>
      a.href = imageId.value; //Image Base64 Goes here
      a.download = textId.value + ".png"; //File name Here
      a.click(); //Downloaded file
    }
}

</script>

<template>
<div>
  <h3>Paint</h3>

  <input type="range" min="10" max="1000" @change="New_page()" v-model="widthId">

  <input type="range" min="10" max="500" @change="New_page()" v-model="heightId">
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

    <button type="button" @click.prevent="Save()">
      submit
    </button>

    <button type="button" @click.prevent="downloadFile()">
      download file
    </button>

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
    margin-left:40%;
    margin-right:40%;

  }


</style>


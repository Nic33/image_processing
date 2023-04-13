<script setup lang="ts">
  import { ref, onMounted } from 'vue';
  import { api } from '@/http-api';
  import { ImageType } from '@/image';
  import { createToast } from 'mosha-vue-toastify';
  import 'mosha-vue-toastify/dist/style.css';

  //-----------------------------------------------//

  const imageId = ref(-1);
  const imageList = ref<ImageType[]>([]);
  getImageList();
  
  const colorId = ref("#000000");

  function getImageList() {
    api.getImageList().then((data) => {
        imageList.value = data;
    }).catch(e => {
      console.log(e.message);
      createToast({ title: ''+ e.message , description: 'Impossible de charger les images'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

    });
  }
  //-----------------------------------------------//

  // declaration de tout mes tableaux

  var tab: [number, number][] = [];
  var index = 0;

  var img_is_put : boolean = false;

  let newArray: [number, number][] = [];
  let Point_de_croisement: [number, number][] = [];

  var last_tab : number[] = [];

  function drawPixel (x:number, y:number) {
    var canvas = <HTMLCanvasElement> document.getElementById("myCanvas");

    console.log("set point at "+ x + " x " + y);

      if (canvas != null){
        var ctx = canvas.getContext("2d");
        
        if (ctx != null){

          ctx.fillStyle = colorId.value;
          ctx.fillRect(x, y, 1, 1);

          tab.push([x, y]);
          index += 1;

          console.log(tab);

          if (index > 1){ //on trace un ligne
            drawLine(tab[index - 2][0], tab[index - 2][1], tab[index - 1][0],tab[index - 1][1]);
          }
        }else{
          console.log("error : function drawPixel()");
          createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

        }
      }else{
          console.log("error : function drawPixel()");
          createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

      }
  }

  var ok_to_draw = true;

  function draw(evt : any) {
    if (ok_to_draw == true){
        var canvas = <HTMLCanvasElement> document.getElementById("myCanvas");

        var rect = canvas.getBoundingClientRect();
        const x = Math.floor(evt.clientX - rect.left);
        const y = Math.floor(evt.clientY - rect.top);

        if (img_is_put == true){

            last_tab.push(x);
            last_tab.push(y);

            drawPixel(x, y);

        }else{

        createToast({ title: 'info', description: "Merci de selectionner une image d'abord"} , {toastBackgroundColor : 'rgb(0,128,0)', type : 'info', timeout : 5000, position : 'top-center', showIcon : true});

        }

    }else{
        console.log('not draw');
    }

  }

  // Dessiner une ligne entre deux points
  function drawLine(x1 :number, y1:number, x2:number, y2:number) {

    console.log("draw x1 = "+ x1 + " -> x2 = " + x2 + " and y1 = "+ y1 + " -> y2 = " + y2);

    var canvas = <HTMLCanvasElement> document.getElementById("myCanvas");
    
    if (canvas != null){
      var ctx = canvas.getContext("2d");

      if (ctx != null){

        ctx.beginPath();
        ctx.moveTo(x1, y1);
        ctx.lineTo(x2, y2);
        ctx.strokeStyle = colorId.value;
        ctx.stroke();

        newArray = newArray.concat(bresenhamLine(x1,y1,x2,y2));
        
        if (tour_complet() == true){

          createToast({ title: 'info', description: "Un croisement à été détecté"} , {toastBackgroundColor : 'rgb(0,128,0)', type : 'info', timeout : 5000, position : 'top-center', showIcon : true});
          ok_to_draw = false;

          cut();

        }else{
          //rien à faire
        }
      }
      else{
        console.log("error : function drawLine()")
        createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});
      }
    }
    else{
      console.log("error : function drawLine()")
      createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});
    }
  }


  function reset_all(){

    tab.splice(0, tab.length); // Supprime toutes les valeurs du tableau
    newArray.splice(0, newArray.length);
    Point_de_croisement.splice(0, Point_de_croisement.length);      
    last_tab.splice(0, last_tab.length);

    index = 0;

    ok_to_draw = true;

    var canvas = <HTMLCanvasElement> document.getElementById("myCanvas");

    if (canvas != null){
        var ctx = canvas.getContext("2d");

        if (ctx != null){
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            ctx.drawImage(base_image, 0, 0);

        }else{
            console.log("error : function reset_all()")
            createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});
        }
    }else{
        console.log("error : function reset_all()")
        createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});
    }

    }
  //Obligé de l'utliser pour detecter le point de croisement
  function bresenhamLine(x0: number, y0: number, x1: number, y1: number): [number, number][] {

    const points: [number, number][] = [];

    let dx = Math.abs(x1 - x0);
    let dy = Math.abs(y1 - y0);
    let sx = (x0 < x1) ? 1 : -1;
    let sy = (y0 < y1) ? 1 : -1;
    let err = dx - dy;
    let x = x0;
    let y = y0;

    while(true) {
        points.push([x, y]);

        if (x === x1 && y === y1) {
            break;
        }

        let e2 = 2 * err;
        if (e2 > -dy) {
            err -= dy;
            x += sx;
        }
        if (e2 < dx) {
            err += dx;
            y += sy;
        }
    }

    points.pop(); // on supprime le dernier élément qui fait doublon

    return points;
}

function tour_complet(): boolean {

  for (var i = 0; i < newArray.length; i++) {
    var value = newArray[i];
    for (var j = (i + 1); j < newArray.length; j++) {

      if (value.every((v, idx) => v === newArray[j][idx])) {
        
        Point_de_croisement.push(newArray[i]);

        console.log("point de croisement : " + Point_de_croisement);

        return true;

      }
    }
  }
  return false;
}

  // non utilisé avec la fonction de Aurel
  function get_tab() :  [number, number][] {

    var new_tab : [number, number][] = [];

    var cpt2 = false;

    console.log("point de croisement : " + Point_de_croisement);

    for (var i = 0; i < newArray.length; i++) {
      
      var tuple1 = newArray[i];
      var tuple2 = Point_de_croisement[0];

      if ((tuple1[0] === tuple2[0]) && (tuple1[1] === tuple2[1])) { // nb égaux

        if (cpt2 == true){  
          new_tab.push(newArray[i])  
          return new_tab;

        }
        cpt2 = true;
      }
      
      if (cpt2 == true) {
        new_tab.push(newArray[i])
      }
    }

    return new_tab;

  }

  //-----------------------------------------------//

  var w = ref(100);
  var h = ref(100);

  var base_image = new Image();

  function setBackgroung(value : boolean){

    reset_all();

    var canvas = <HTMLCanvasElement> document.getElementById("myCanvas");
    var select = document.getElementById("select");

    var ctx = canvas.getContext("2d");

    img_is_put = false;

    // on charge l'image 
    if (value == false){

      if (canvas != null && select != null){
        
        canvas.style.display = 'inline';

        createToast({ title: 'info', description: 'Please wait'} , {toastBackgroundColor : 'rgb(0,128,0)', type : 'info', timeout : 2000, position : 'top-center', showIcon : true});

        base_image.src = 'images/'+ imageId.value.toString();

        select.style.display = "none"

        setTimeout(function() {

          w.value = base_image.width;
          h.value = base_image.height;

          console.log(w.value)

        }, 2000);

        setTimeout(function() {

          console.log("affiche")

          setBackgroung(true);

        }, 2500);

      }
      else{
        console.log("error : function setBackgroung()")
        createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

      }

    }else{ // on affiche l'image

      if (canvas != null && select != null){
        
        if (ctx != null){
          ctx.drawImage(base_image, 0, 0);

        }else{
          console.log("error : function setBackgroung()")
          createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

        }

        select.style.display = "inline"

      }else{
        console.log("error : function setBackgroung()")
        createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

      }

      img_is_put = true;

    }

  }

  //-----------------------------------------------//

  async function cut(){

    console.log("last_tab = "+ last_tab);

    await api.ChangeImage(imageId.value, "Cut" , -1 , -1, last_tab); 

    //location.reload();
    createToast({ title: 'info', description: "L'image a été modifié, vous pouvez la visualiser dans la galerie"} , {toastBackgroundColor : 'rgb(0,128,0)', type : 'info', timeout : 5000, position : 'top-center', showIcon : true});

  }

</script>

<template>
<div>

  <h3>Contour</h3>
  <div id ="panel">
        
    <p id ="note">L'objectif de cette partie est de supprimer la partie de l'image que vous aurez selectionné au préalable.</p>

  </div>

  <br>

  <canvas id="myCanvas" :width="w" :height="h" @click.prevent="draw($event)"></canvas>
  
  <br>

  <div id = "controle">

    <div id = "select">
      <select  v-model="imageId" @change="setBackgroung(false)">
        <option v-for="image in imageList" :value="image.id" :key="image.id">
          {{ image.name }}
        </option>
      </select>
      <br>
      <br>
    </div>


    <input type="color" v-model="colorId">  

    <button id = "reset" @click="reset_all()">
        reset
    </button>

<!--     <div id = "cut">

      <br>
      <br>
      <button @click="cut()">
        couper l'image
      </button>
    </div>
 -->
  </div>

</div> 
</template>

<style scoped>

  canvas{
    border: solid 1px rgb(0, 0, 0);
    display: none;
  }


  #cut{
    display: none;
  }

  #controle{
    width: 300px;
    margin-left: auto;
    margin-right: auto;
    background-color: rgb(218, 218, 218);
    color: white;
    border-radius: 10px;
    padding: 20px;
    padding-bottom: 20px;

    margin-bottom: 20px;
  }


  #panel{
    max-width: 600px;
    margin-left: auto;
    margin-right: auto;
    background-color: rgb(218, 218, 218);
    color: rgb(0, 0, 0);
    border-radius: 10px;
    padding: 20px;
    padding-bottom: 20px;
  }

  #reset{
    margin:10px;
  }

</style>


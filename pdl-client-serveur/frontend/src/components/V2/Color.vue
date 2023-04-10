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
    const p1 = ref(78);

    function getImageList() {
        api.getImageList().then((data) => {
            imageList.value = data;
        }).catch(e => {
        console.log(e.message);
        createToast({ title: ''+ e.message , description: 'Impossible de charger les images'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

        });
    }


    var w = ref(100);
    var h = ref(100);

    var base_image = new Image();

    var img_is_put : boolean = false;

    function setBackgroung(value : boolean){

        tab.splice(0, tab.length);

        var btn_wait = document.getElementById("wait");
        var canvas = <HTMLCanvasElement> document.getElementById("myCanvas");
        var select = document.getElementById("select");

        var ctx = canvas.getContext("2d");

        img_is_put = false;

        // on charge l'image 
        if (value == false){

            if (canvas != null && btn_wait != null && select != null){
                
                canvas.style.display = 'inline';

                createToast({ title: 'info', description: 'Please wait'} , {toastBackgroundColor : 'rgb(0,128,0)', type : 'info', timeout : 2000, position : 'top-center', showIcon : true});

                base_image.src = 'images/'+ imageId.value.toString();

                select.style.display = "none"

                setTimeout(function() {

                if (btn_wait != null){

                    createToast({ title: 'info', description: "Appuyer sur le bouton afficher pour visualiser l'image"} , {toastBackgroundColor : 'rgb(0,128,0)', type : 'info', timeout : 5000, position : 'top-center', showIcon : true});

                    btn_wait.style.display = "inline";

                }

                w.value = base_image.width;
                h.value = base_image.height;

                console.log(w.value)

                }, 2000);

            }
            else{
                console.log("error : function setBackgroung()")
                createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

            }

        }else{ // on affiche l'image

            if (canvas != null && btn_wait != null && select != null){
                
                if (ctx != null){
                ctx.drawImage(base_image, 0, 0);

                }else{
                console.log("error : function setBackgroung()")
                createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

                }

                btn_wait.style.display = "none";        

                select.style.display = "inline"

            }else{
                console.log("error : function setBackgroung()")
                createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

            }

            img_is_put = true;

        }

    }

    //-----------------------------------------------//

    var tab : number[] = [];

    async function color(){

      if (imageId.value != -1){

        console.log(hexToRgb(colorId.value));

        const colorRgb = hexToRgb(colorId.value);
        if (colorRgb) {
            tab.push(colorRgb.r);
            tab.push(colorRgb.g);
            tab.push(colorRgb.b);

        }
        await api.ChangeImage(imageId.value, "KeepColor" , p1.value, -1, tab); 

        createToast({ title: 'info', description: "L'image a été modifié, vous pouvez la visualiser dans la galerie"} , {toastBackgroundColor : 'rgb(0,128,0)', type : 'info', timeout : 5000, position : 'top-center', showIcon : true});
      }else{

        console.log("error : function color()")
        createToast({ title: 'error', description: 'Merci de sélectionner une couleur et une image'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});
      }
    }

    //-----------------------------------------------//

    function getColorAtMousePosition(event : any) {

        var canvas = <HTMLCanvasElement> document.getElementById("myCanvas");

        var rect = canvas.getBoundingClientRect();
        var x = event.clientX - rect.left;
        var y = event.clientY - rect.top;
        var ctx = canvas.getContext('2d');
        var pixelData = ctx?.getImageData(x, y, 1, 1).data;

        if (pixelData != null){

            var color = 'rgb(' + pixelData[0] + ',' + pixelData[1] + ',' + pixelData[2] + ')';
            console.log(color);

            colorId.value = rgbToHex(pixelData[0], pixelData[1], pixelData[2]);

        }else{

            console.log("error : function getColorAtMousePosition()")
            createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

        }

    }

    function hexToRgb(hex: string): { r: number; g: number; b: number } | null {

        const match = hex.match(/^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i);
        if (!match) {
            return null;
        }
        const r = parseInt(match[1], 16);
        const g = parseInt(match[2], 16);
        const b = parseInt(match[3], 16);
        return { r, g, b };

    }

    function rgbToHex(r: number, g: number, b: number): string {
        const componentToHex = (c: number) => {
            const hex = c.toString(16);
            return hex.length === 1 ? "0" + hex : hex;
        };
        return "#" + componentToHex(r) + componentToHex(g) + componentToHex(b);
    }


    function color_selected(){

        console.log(colorId.value);

    }

    onMounted(() => { // super utile
    
        color_selected();

    })


</script>

<template>
<div>

  <h3>Color</h3>
  <div id ="panel">
        
    <p id ="note">L'objectif de cette partie est de garder une couleur de l'image que vous aurez sélectionné au préalable</p>

  </div>

  <br>

  <canvas id="myCanvas" :width="w" :height="h" @click.prevent="getColorAtMousePosition($event)"></canvas>
  
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

    <input type="color" v-model="colorId" @change = "color_selected">  

    <br>
    <br>

    <input type="range" min = "0" max = "314" step="1" v-model="p1">

    <div id = "wait">

      <br>
      <br>
      <button @click="setBackgroung(true)">
        afficher l'image
      </button>

    </div>

    <br>
    <br>

    <button @click="color()">
        Envoyer
    </button>

  </div>

</div> 
</template>

<style scoped>

  canvas{
    border: solid 1px rgb(0, 0, 0);
    display: none;
  }

  #wait{
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


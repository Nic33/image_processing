<script setup lang="ts">
import { ref } from 'vue';
import router from "@/router";
import { api } from '@/http-api';
import { ImageType } from '@/image'
import { createToast } from 'mosha-vue-toastify';
import 'mosha-vue-toastify/dist/style.css';

const imageId = ref(-1);
const imageList = ref<ImageType[]>([]);
getImageList();

const props = defineProps<{ id: number }>()

api.getImage(props.id)
  .then((data: Blob) => {
    const reader = new window.FileReader();
    reader.readAsDataURL(data);
    reader.onload = () => {
      const galleryElt = document.getElementById("gallery-" + props.id);
      if (galleryElt !== null) {
        const imgElt = document.createElement("img");
        imgElt.style.width = '50%';
        imgElt.style.minWidth = '300px'
        galleryElt.appendChild(imgElt);
        if (imgElt !== null && reader.result as string) {
          imgElt.setAttribute("src", (reader.result as string));
        }
      }
    };
  })
  .catch(e => {
    console.log(e.message);
    createToast({ title: '' + e.message, description: 'Impossible de charger les images' }, { toastBackgroundColor: 'rgb(255,0,0)', type: 'danger', timeout: 5000, position: 'top-center', showIcon: true });

  });


function getImageList() {
  api.getImageList().then((data) => {
    imageList.value = data;
  }).catch(e => {
    console.log(e.message);
    createToast({ title: '' + e.message, description: 'Impossible de charger les images' }, { toastBackgroundColor: 'rgb(255,0,0)', type: 'danger', timeout: 5000, position: 'top-center', showIcon: true });

  });
}

// image après ---------------------------

const p1 = ref(-1);
const p2 = ref(-1);

function change() {

  const image = imageList.value.find(img => img.id === props.id);

  let selectedFunction = null;

  if (image != null) {

    if (image.color == true) {
      selectedFunction = functions_color.find(f => f.id === parseInt(functionId.value));
    } else if (image.color == false) {
      selectedFunction = functions_gray.find(f => f.id === parseInt(functionId.value));
    }
  } else {
    console.log("error : function change()")
    createToast({ title: 'error', description: 'Image introuvable' }, { toastBackgroundColor: 'rgb(255, 0, 0)', type: 'danger', timeout: 5000, position: 'top-center', showIcon: true });

  }

  if (selectedFunction != null) {
    console.log("Image id : " + props.id + " fonction : " + selectedFunction.name + " value 1 : " + p1.value + " value 2 : " + p2.value);

    if (selectedFunction.param == 1 && p1.value != -1) { // on verifie que les parametres soient bons

      return api.ChangeImage(props.id, selectedFunction.name, p1.value, p2.value, []);

    } else if (selectedFunction.param == 2 && p1.value != -1 && p2.value != -1) { // on verifie que les parametres soient bons

      return api.ChangeImage(props.id, selectedFunction.name, p1.value, p2.value, []);

    } else if (selectedFunction.param == 0) { // on verifie que les parametres soient bons

      return api.ChangeImage(props.id, selectedFunction.name, p1.value, p2.value, []);

    } else {
      createToast({ title: 'info', description: 'Vos paramètres ne sont pas bons' }, { toastBackgroundColor: 'rgb(0, 128, 0)', type: 'info', timeout: 5000, position: 'top-center', showIcon: true });

      return null;
    }

  } else {
    console.log("Function not found for id: " + functionId.value);
    createToast({ title: 'error', description: 'fonction introuvable' }, { toastBackgroundColor: 'rgb(255, 0, 0)', type: 'danger', timeout: 5000, position: 'top-center', showIcon: true });
    return null;
  }

}

async function showImage() {

  await change();
  await router.push({ name: 'modify' }); // obligé de retourner sur l'autre router pour que ca se mette à jour x)
  await router.push({ name: 'modify_id', params: { id: props.id } });

}

// fonctions ---------------------------

const functionId = ref("");

const functions_color = [
  { id: 0, name: "FiltreColor", param: 1, note: "Mettre une valeur entre 0 et 360" },
  { id: 1, name: "FiltreFlou", param: 1, note: "Mettre 0 si vous souhaitez un filtre moyenneur ou 1 pour un filtre gaussien" },
  { id: 2, name: "FiltreContour", param: 0, note: "Cette fonction n'a pas besoin de valeur" },
  { id: 3, name: "Luminosite", param: 1, note: "Mettre une valeur entre 0 et 255" },
  { id: 4, name: "EgalHisto", param: 0, note: "Cette fonction n'a pas besoin de valeur" },
  { id: 5, name: "Gray", param: 0, note: "Cette fonction n'a pas besoin de valeur" },
  { id: 6, name: "Rotate", param: 1, note: "Mettre une valeur entre 0° et 360°" }
];

const functions_gray = [
  { id: 0, name: "FiltreFlou", param: 1, note: "Mettre 0 si vous souhaitez un filtre moyenneur ou 1 pour un filtre gaussien" },
  { id: 1, name: "FiltreContour", param: 0, note: "Cette fonction n'a pas besoin de valeur" },
  { id: 2, name: "Luminosite", param: 1, note: "Mettre une valeur entre 0 et 255" },
  { id: 3, name: "EgalHisto", param: 0, note: "Cette fonction n'a pas besoin de valeur" },
  { id: 4, name: "Rotate", param: 1, note: "Mettre une valeur entre 0° et 360°" }
];

function mesNotes() {
  const varNote = document.getElementById("note");

  const inp1 = document.getElementById("inp1");
  const inp2 = document.getElementById("inp2");

  const panel = document.getElementById("panelBis");

  if (panel != null) {
    panel.style.display = "inline";

  } else {

    console.log("error : function mesNotes()");
    createToast({ title: 'error', description: 'fonction introuvable' }, { toastBackgroundColor: 'rgb(255, 0, 0)', type: 'danger', timeout: 5000, position: 'top-center', showIcon: true });

  }
  const image = imageList.value.find(img => img.id === props.id); // on vient chercher les informations de l'image

  let selectedFunction = null;

  if (image != null) {

    if (image.color == true) {
      selectedFunction = functions_color.find(f => f.id === parseInt(functionId.value));

    } else if (image.color == false) {
      selectedFunction = functions_gray.find(f => f.id === parseInt(functionId.value));
    }

  } else {
    console.log("error image not found")
    createToast({ title: 'error', description: 'image introuvable' }, { toastBackgroundColor: 'rgb(255, 0, 0)', type: 'danger', timeout: 5000, position: 'top-center', showIcon: true });

  }

  if (varNote != null && selectedFunction != null) {

    varNote.innerHTML = "" + selectedFunction.note;

  } else {
    console.log("error id note or id submit");
    createToast({ title: 'error', description: 'id introuvable' }, { toastBackgroundColor: 'rgb(255, 0, 0)', type: 'danger', timeout: 5000, position: 'top-center', showIcon: true });

  }

  if (inp1 != null && inp2 != null) {

    if (selectedFunction != null) {
      if (selectedFunction.param == 0) {
        inp1.style.display = "none";
        inp2.style.display = "none";

      }
      else if (selectedFunction.param == 1) {
        inp1.style.display = "inline";
        inp2.style.display = "none";

      }
      else if (selectedFunction.param == 2) {
        inp1.style.display = "inline";
        inp2.style.display = "inline";

      }
      else {
        console.log("error p1 p2 input");
        createToast({ title: 'error', description: 'Une erreur est survenue pour les paramètres p1 et p2' }, { toastBackgroundColor: 'rgb(255, 0, 0)', type: 'danger', timeout: 5000, position: 'top-center', showIcon: true });

      }
    }
    else {
      console.log("error selected Function");
      createToast({ title: 'error', description: 'Une erreur est survenue pour les paramètres p1 et p2' }, { toastBackgroundColor: 'rgb(255, 0, 0)', type: 'danger', timeout: 5000, position: 'top-center', showIcon: true });

    }

  } else {
    console.log("error p1 and p2 are null");
    createToast({ title: 'error', description: 'Une erreur est survenue pour les paramètres p1 et p2' }, { toastBackgroundColor: 'rgb(255, 0, 0)', type: 'danger', timeout: 5000, position: 'top-center', showIcon: true });

  }

}

// image avant ---------------------------

async function afficheImage() {

  await router.push({ name: 'modify' }); // obligé de retourner sur l'autre router pour que ca se mette à jour 
  await router.push({ name: 'modify_id', params: { id: imageId.value } });

}

function isColor(): boolean {
  const image = imageList.value.find(img => img.id === props.id);
  if (image) {
    return image.color;
  }
  return false;
};

function getInfo() {

  console.log("info");

  const image = imageList.value.find(img => img.id === props.id);

  if (image) {

    createToast({
      title: 'info', description: "name : " + image.name + ", " +
        "type : " + image.type + ", " +
        "size : " + image.size + ", " +
        "color : " + image.color
    },
      { toastBackgroundColor: 'rgb(0, 128, 0)', type: 'info', timeout: 5000, position: 'top-center', showIcon: true });


  } else {
    console.log("error : function getInfo()")
    createToast({ title: 'error', description: 'Image introuvable' }, { toastBackgroundColor: 'rgb(255, 0, 0)', type: 'danger', timeout: 5000, position: 'top-center', showIcon: true });

  }

}

// delete ---------------------------

async function Delete() {
  console.log("Delete");
  createToast({ title: 'info', description: "L'image a été supprimée 😘" }, { toastBackgroundColor: 'rgb(0, 128, 0)', type: 'info', timeout: 5000, position: 'top-center', showIcon: true });
  await api.deleteImage(props.id);
  await router.push({ name: 'modify' });

}

//script

</script>

<template>
  <div>
    <h3>Modify an image</h3>

    <div id="panel">
      <p>
        Choisissez une image
      </p>
      <select v-model="imageId" @change="afficheImage()">
        <option v-for="image in imageList" :value="image.id" :key="image.id">
          {{ image.name }}
        </option>
      </select>

      <br>
      <hr>

      <p>Choisissez une fonction</p>

      <select v-if="isColor() == true" v-model="functionId" @change="mesNotes()">
        <option v-for="item in functions_color" :value="item.id" :key="item.id">
          {{ item.name }}
        </option>
      </select>

      <select v-if="isColor() == false" v-model="functionId" @change="mesNotes()">
        <option v-for="item in functions_gray" :value="item.id" :key="item.id">
          {{ item.name }}
        </option>
      </select>

      <div id="panelBis">

        <hr>

        <p id="note"></p>

        <input id="inp1" type="number" v-model="p1">

        <input id="inp2" type="number" v-model="p2">

        <button id="submit" v-on:click="showImage"> modify </button>

      </div>

    </div>

    <figure :id="'gallery-' + id"></figure>

    <div id="panel2">

      <a @click="Delete()">
        <span class="material-symbols-outlined">
          delete
        </span>
      </a>

      <a style="text-indent: 2em" @click="getInfo()">
        <span class="material-symbols-outlined">
          info
        </span>
      </a>

    </div>
  </div>
</template>

<style scoped>
input {
  width: 60px;
  display: none;
}

hr {
  width: 50%;

}

#panel {
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  background-color: black;
  color: white;
  border-radius: 10px;
  padding: 20px;
  padding-bottom: 30px;
}

#panel2>a {
  color: black;
}

#panelBis {
  display: none;
}
</style>


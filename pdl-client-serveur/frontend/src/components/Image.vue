<script setup lang="ts">
  import { defineProps } from 'vue';
  import { api } from '@/http-api';
  import router from "@/router";
  import { ref } from 'vue';
  import { ImageType } from '@/image'
  import { createToast } from 'mosha-vue-toastify';
  import 'mosha-vue-toastify/dist/style.css';

  const props = defineProps<{ id: number}>();

  const selectedId = ref(-1);

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
   // Créez une fonction pour mettre à jour l'image
   async function updateImage(id: number) {
    try {
      const data: Blob = await api.getImage(id);
      const reader = new window.FileReader();
      reader.readAsDataURL(data);
      reader.onload = () => {
        const galleryElt = document.getElementById("gallery-" + id);
        if (galleryElt !== null) {
          let imgElt = galleryElt.querySelector("img");
          if (imgElt === null) {
            imgElt = document.createElement("img");
            imgElt.id = "gal";
            galleryElt.appendChild(imgElt);
          }
          if (imgElt !== null && reader.result as string) {
            imgElt.setAttribute("src", reader.result as string);
          }
        }
      };
    } catch (e) {
      console.log(e);
      createToast({ title: '' + e, description: 'Impossible de charger les images' }, { toastBackgroundColor: 'rgb(255,0,0)', type: 'danger', timeout: 5000, position: 'top-center', showIcon: true });
    }
  }


  updateImage(props.id);

  async function showImage() {
    await router.push({ name: 'image', params: { id: selectedId.value } });
    updateImage(selectedId.value); 
  }

</script>

<template>

  <h3>Choose an image</h3>
  <div>
    <select v-model="selectedId" @change="showImage">
      <option v-for="image in imageList" :value="image.id" :key="image.id">{{ image.name }}</option>
    </select>
  </div>
  <figure :id="'gallery-'+id"></figure>  
</template>

<style>

  #gal{
    width: 500px;
    transition: 0.2s;
    margin: 20px;

  }

  #gal:hover{
    transition: 0.2s;
    width: 600px;
    border-radius: 20px;
  }

  @media (max-width:800px) {

    #gal{
      width: 80%;
    }

    #gal:hover{
      width: 90%;
    }

  }
</style>
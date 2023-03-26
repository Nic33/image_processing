<script setup lang="ts">
    import { ref } from 'vue';
    import { api } from '@/http-api';
    import { ImageType } from '@/image';
    import { createToast } from 'mosha-vue-toastify';
    import 'mosha-vue-toastify/dist/style.css';

    const imageId = ref(-1);
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
    

    function getImageName(id: number): string {
        const image = imageList.value.find(img => img.id === id);
        if (image) {
          return image.name;
        } else {
          return "undefined";
        }
    }

  function Download(){
    console.log("Download");
    
    api.getImage(imageId.value)
      .then((data: Blob) => {
        const reader = new window.FileReader();
        reader.readAsDataURL(data);
        const link = document.createElement('a');
        link.href = URL.createObjectURL(data);
        console.log("Download -> id : "+ imageId.value + ", name : "+ getImageName(imageId.value));
        link.download = getImageName(imageId.value);
        link.click();
        URL.revokeObjectURL(link.href);
      })
      .catch(e => {
        console.log(e.message);
        createToast({ title: ''+ e.message , description: "Impossible de télécharger l'image"} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

      });
  }

</script>

<template>
  <div>
    <h3>Download an image</h3>

    <select v-model="imageId">
          <option v-for="image in imageList" :value="image.id" :key="image.id">
            {{ image.name }}
          </option>
    </select>

    <br><br>

    <button v-on:click="Download()"> Download </button>

  </div>
</template>

<style scoped>
</style>

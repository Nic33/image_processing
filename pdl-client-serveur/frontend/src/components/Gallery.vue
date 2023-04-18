<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '@/http-api';
import { ImageType } from '@/image'
import { createToast } from 'mosha-vue-toastify';
import 'mosha-vue-toastify/dist/style.css';

const imageList = ref<ImageType[]>([]);

async function getImageList() {
  try {
    const data = await api.getImageList();
    imageList.value = data;
  } catch (e) {
    console.log(e);
    createToast({ title: ''+ e , description: 'Impossible de charger les images'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});
  }
}

getImageList();

</script>

<template>
  <div>
    <h3>Gallery</h3>
    <div id="gallery">
      <img v-for="image in imageList" :key="image.id" class="gallery-image" :src="'images/' + image.id"/>
    </div>
  </div>
</template>

<style scoped>

#gallery{
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  margin-top: 80px;
  margin-bottom: 80px;
}

.gallery-image{
  width : 300px;
  height: 300px;
  object-fit: cover;
  transition: 0.2s;
  border-radius: 20px;

  margin : 20px;
}

.gallery-image:hover{
  border-radius: 0px;
  transition: 0.2s;
  transform: scale(1.3);
}



</style>

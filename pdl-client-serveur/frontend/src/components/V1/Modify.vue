    <script setup lang="ts">
    import { ref } from 'vue';
    import router from "@/router";
    import { api } from '@/http-api';
    import { ImageType } from '@/image'
    import { createToast } from 'mosha-vue-toastify';
    import 'mosha-vue-toastify/dist/style.css';

    // images ---------------------------

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


    function afficheImage(){
      router.push({ name: 'modify_id', params: { id: imageId.value } })

    }

    // fonctions ---------------------------

  
  </script>
  
  <template>
    <div>
      <h3>Modify an image</h3>
      <div>

        <select v-model="imageId" @change="afficheImage">
          <option v-for="image in imageList" :value="image.id" :key="image.id">
            {{ image.name }}
          </option>
        </select>

      </div>
    </div> 
  </template>
  
  <style scoped>

  input{
    width: 60px;
  }
  </style>
  

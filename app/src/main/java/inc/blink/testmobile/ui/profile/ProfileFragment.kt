package inc.blink.testmobile.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import inc.blink.testmobile.R
import inc.blink.testmobile.databinding.FragmentProfileBinding
import java.util.UUID

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            if (imageUri != null) {
                uploadImageToFirebase(imageUri)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        updateUI()

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.loginFragment)
        }

        binding.ivProfilePic.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }
    }



    private fun updateUI() {
        val user = auth.currentUser
        binding.tvEmail.text = user?.email ?: "Неизвестно"
        
        user?.photoUrl?.let {
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.ic_profile)
                .into(binding.ivProfilePic)
        }
    }
    private fun uploadImageToFirebase(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val fileName = UUID.randomUUID().toString()
        val ref = storage.reference.child("profile_pics/$uid/$fileName")

        binding.ivProfilePic.alpha = 0.5f // Dim while uploading
        
        ref.putFile(uri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { downloadUri ->
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(downloadUri)
                    .build()

                auth.currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        binding.ivProfilePic.alpha = 1.0f
                        updateUI()
                        Toast.makeText(requireContext(), "Фото обновлено", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.addOnFailureListener {
            binding.ivProfilePic.alpha = 1.0f
            Toast.makeText(requireContext(), "Ошибка загрузки: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

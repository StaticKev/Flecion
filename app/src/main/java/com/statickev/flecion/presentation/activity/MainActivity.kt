package com.statickev.flecion.presentation.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.statickev.flecion.R
import com.statickev.flecion.databinding.ActivityMainBinding
import com.statickev.flecion.presentation.fragments.JournalFragment
import com.statickev.flecion.presentation.fragments.FinanceFragment
import com.statickev.flecion.presentation.fragments.RecurringTasksFragment
import com.statickev.flecion.presentation.fragments.TaskFragment
import com.statickev.flecion.presentation.presentationUtil.generalSetup
import com.statickev.flecion.util.AppPrefs
import com.statickev.flecion.util.getDayDateFormatter
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        generalSetup(this)

        binding = ActivityMainBinding.inflate(layoutInflater)

        if (AppPrefs.isFirstLaunch(this)) {
            askNotificationPermission()
        }

        with (binding) {
            topBar.btDate.text = LocalDate.now().format(getDayDateFormatter())
            topBar.btnMenu.setOnClickListener {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                else drawerLayout.openDrawer(GravityCompat.START)
            }

            // TODO: Attach onClickListener on navigation drawer.
            nvMenu.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_my_tasks -> { navigateToFragment(TaskFragment()) }
                    R.id.nav_recurring -> { navigateToFragment(RecurringTasksFragment()) }
                    R.id.nav_journal -> { navigateToFragment(JournalFragment()) }
                    R.id.nav_finance -> { navigateToFragment(FinanceFragment()) }
                }

                if (menuItem.isCheckable) menuItem.isChecked = true
                drawerLayout.closeDrawers()
                true
            }
        }

        setContentView(binding.root)
    }

    // TODO: STILL NOT TESTED!
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fcMain.id, fragment)
            .commit()
    }

}
package com.mattam.hacha.src.main.search

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mattam.hacha.R
import com.mattam.hacha.databinding.FragmentSearchBinding
import com.mattam.hacha.src.main.MainActivity
import com.mattam.hacha.src.main.MainViewModel
import com.mattam.hacha.src.main.dto.Feed
import com.mattam.template.config.BaseFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SearchFragment : BaseFragment<FragmentSearchBinding>(FragmentSearchBinding::bind, R.layout.fragment_search) {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mainActivity: MainActivity
    lateinit var searchView: androidx.appcompat.widget.SearchView
    private val activityViewModel: MainViewModel by activityViewModels()
    lateinit var searchAdapter: SearchAdapter
    lateinit var searchRecyclerView: RecyclerView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("차선호", "search에서 list: ${activityViewModel.searchFeedList}")

        binding.apply {
            searchRecyclerView = searchRecyclerview
            setSearchRecyclerView()

            searchView = searchview
            searchView.setQuery(activityViewModel.searchText,false)
            searchView.isIconified = false
            searchView.setOnQueryTextListener(object: androidx.appcompat.widget.SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    activityViewModel.searchText = query ?: ""
                    if(query!=null){
                        activityViewModel.searchFeedList = arrayListOf()
                        var searchContext = query.trim()
                        CoroutineScope(Dispatchers.Main).launch {
                            Feedsearch(searchContext)
                        }
                    }
                    // 키보드 숨기기
                    val inputMethodManager = mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(searchView.windowToken, 0)

                    return true
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }
            })
        }

    }

    private fun setSearchRecyclerView(){
        searchAdapter = SearchAdapter(activityViewModel.searchFeedList)
        searchRecyclerView.layoutManager  = GridLayoutManager(mainActivity, 3)
        searchRecyclerView.adapter = searchAdapter.apply {
            itemClickListner = object : SearchAdapter.ItemClickListener{
                override fun onClick(view: View, position: Int, feed: Feed) {
                    //해당 피드의 stroeInfoFragment로 이동
                    activityViewModel.selectedSearchFeedPosition = position
                    activityViewModel.storeInfoFeed = feed
                    mainActivity.changeFragment("storeInfo")
                }
            }
        }
    }

    private fun Feedsearch(searchContent :String){
        activityViewModel.apply {
            for((index, value) in feedList.value!!.withIndex()){
                Log.d("MainActivity", "getFeedInfo chadong: ${value}")
                value.apply {
                    if(description.contains(searchContent)){
                        searchFeedList.add(value)
                    }else if(storeLocation.contains(searchContent)){
                        searchFeedList.add(value)
                    }else if(storeName.contains(searchContent)){
                        searchFeedList.add(value)
                    }
                }
            }
            setSearchRecyclerView()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
import React, { useContext, useEffect, useState } from 'react'
import style from "../styles/header.module.css"
import { FaSearch } from "react-icons/fa";
import { TbPhotoSearch } from "react-icons/tb";
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

import { API_BASE_URL } from '../utils/constants';

import { SearchContext } from './SearchProvider';

export default function Header({ getterIsLogin, headerUserInfo }) {
  const { setSearchState } = useContext(SearchContext);

  const navigate = useNavigate();

  const [query, setQuery] = useState("");

  const handleLogout = () => {
    // 토큰 삭제    
    localStorage.removeItem('token');
    localStorage.removeItem('isLogin');

    // window.location.reload();
    // 로그아웃 후 메인 페이지로 이동
    navigate("/main");
    window.location.reload();
  };

  const requestProfileImageURL = (profileImage) => {
    console.log("프로필 사진: ", profileImage);
    console.log("프로필 사진: ", headerUserInfo.profileImage);
    const profileImageURL = `${API_BASE_URL}/api/profileImagePath/` + profileImage;
    console.log(profileImageURL);
    return profileImageURL;
  };

  const handleSearch = async () => {
    if (query) {
      navigate("/searchResult", { state: {query: query} });
    } else {
      alert("검색어를 입력하세요.");
    }
  }

  const handleImageSearch = () => {
    setSearchState({
      query: '',
      searchImage: null,
      similarityImageProduct: [],
    });
    navigate("/imageSearchResult");
  };

  return (
    <div className={style.header_box}>
      <div className={style.header_top}>
        <div className={style.header_logo}>
          <Link className={style.main_page_logo} to={'/main'} onClick={() => {setQuery("")}}>
            <h1>대여돼요</h1>
          </Link>
        </div>
        <div className={style.header_search}>
          <div className={style.search_box}>
            {/* <div className='search-input-box'>
              <input type="text" />
            </div> */}
            <label className={style.search_input_label}>
              <input type="text" className={style.search_input} onChange={(e) => {setQuery(e.target.value)}} value={query} placeholder='검색어를 입력하세요.' onKeyDown={(e) => {if (e.key === "Enter") {handleSearch();}}}/>
            </label>
            <label className={`${style.search_button_label} ${style.querySearch_button_label}`}>
              <button className={`${style.search_button} ${style.querySearch_button}`} onClick={() => {handleSearch();}}>
                <FaSearch />
              </button>
            </label>
            <label className={`${style.search_button_label} ${style.imageSearch_button_label}`}>
              <button className={`${style.search_button} ${style.imageSearch_button}`} onClick={() => {handleImageSearch();}}>
                <TbPhotoSearch />
              </button>
            </label>
          </div>
        </div>
        <div className={style.header_user}>
          {{getterIsLogin} && headerUserInfo ? (
            <>
            <div className={style.user_image_box}>
              <div className={style.user_image}>
                <img src={requestProfileImageURL(headerUserInfo.profileImage)} alt='프로필 사진' />
              </div>
            </div>
            <div className={style.user_info_box}>
              <div className={style.user_nickname}>{headerUserInfo.nickName}님</div>
              <button className={style.logout_button} onClick={handleLogout}>로그아웃</button>
            </div>
            </>
          ) : (
            <>
              <div className={style.user_image_box}>
                <div className={style.user_image}>
                  <img src="https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png" alt='프로필 사진' />
                </div>
              </div>
              <div className={style.user_info_box}>
                <div className={style.user_nickname}>게스트</div>
                <Link className={style.login_button} to={'/login'}>로그인</Link>
              </div>
            </>

          )}
        </div>
      </div>
      <div className={style.header_nav}>
        <ul>
          <li>
            <Link className={style.nav_menu} to={'/shorts'}>쇼츠</Link>
          </li>
          {/* <li>
            <Link className={style.nav_menu} to={'/'}>Q&A</Link>
          </li> */}
          <li>
            {/* <Link className={style.nav_menu} to={'/myPage'}>마이페이지</Link> */}
            <button className={style.nav_menu} onClick={() => {getterIsLogin ? navigate('/myPage'):navigate('/login')}}>마이페이지</button>
          </li>
          <li>
            <Link className={style.nav_menu} to={'/product'}>상품등록</Link>
          </li>
          
          {/* route 확인
          <li>
            <Link className={style.nav_menu} to={'/login'}>로그인</Link>
          </li> */}
        </ul>
      </div>
    </div>
  )
}
